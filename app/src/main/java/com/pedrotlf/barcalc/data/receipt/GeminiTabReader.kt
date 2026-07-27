package com.pedrotlf.barcalc.data.receipt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.core.net.toUri
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.ImagePart
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import com.pedrotlf.barcalc.domain.receipt.ParsedItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Reads a tab with Gemini Nano, on-device.
 *
 * The line parser has to be told what a tab looks like, one rule at a time,
 * and real tabs keep disagreeing with it. A model that sees the photo can
 * follow the layout instead — but it is also free to invent, so everything it
 * returns is checked before it gets anywhere near the tab, and the review
 * screen is what the user actually confirms.
 *
 * Only available on the devices that carry Nano; elsewhere this reports a
 * failure and the chain falls through to text recognition.
 */
class GeminiTabReader(private val context: Context) : TabReader {

    private val model by lazy { Generation.getClient() }
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** Outlives any one scan, because fetching the model does too. */
    private val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var downloadRequested = false

    override suspend fun read(imageUri: String): Result<TabReading> = runCatching {
        when (model.checkStatus()) {
            FeatureStatus.AVAILABLE -> Unit

            FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> {
                // Fetching the model takes far too long to hold up a scan, so
                // it's started once in the background and this scan falls
                // through to text recognition. The next one gets the better
                // reading.
                startDownloadOnce()
                error("Gemini Nano is still being prepared")
            }

            else -> error("Gemini Nano isn't available on this device")
        }

        val bitmap = withContext(Dispatchers.IO) { loadBitmap(imageUri) }
        val response = model.generateContent(
            generateContentRequest(ImagePart(bitmap), TextPart(Prompt)) {},
        )
        val text = response.candidates.firstOrNull()?.text.orEmpty()
        TabReading(parseItems(text), text, ReadingSource.MODEL)
    }

    private fun startDownloadOnce() {
        if (downloadRequested) return
        downloadRequested = true
        downloadScope.launch {
            // Collecting drives the download; failures just mean the next scan
            // tries again, so there's nothing to report here.
            runCatching { model.download().collect { } }
        }
    }

    private fun loadBitmap(imageUri: String): Bitmap {
        val source = ImageDecoder.createSource(context.contentResolver, imageUri.toUri())
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.isMutableRequired = false
            // Full-resolution scans are far larger than the model needs and
            // risk running the app out of memory on the way in.
            val longest = maxOf(info.size.width, info.size.height)
            if (longest > MaxImageEdge) {
                val scale = MaxImageEdge.toFloat() / longest
                decoder.setTargetSize(
                    (info.size.width * scale).toInt().coerceAtLeast(1),
                    (info.size.height * scale).toInt().coerceAtLeast(1),
                )
            }
        }
    }

    /**
     * Pulls items out of the model's reply, keeping only what could plausibly
     * be on a tab. Anything malformed, free, or absurdly priced is dropped
     * rather than shown — an invented line is the one failure the user has no
     * way to spot as invented.
     */
    private fun parseItems(response: String): List<ParsedItem> {
        val body = response.substringAfter('{', "").substringBeforeLast('}', "")
        if (body.isBlank()) return emptyList()
        val decoded = runCatching {
            json.decodeFromString<ModelReply>("{$body}")
        }.getOrNull() ?: return emptyList()

        return decoded.items.mapNotNull { item ->
            val name = item.name.trim()
            if (name.isEmpty()) return@mapNotNull null
            if (item.unitPriceCents !in 1..MaxItemCents) return@mapNotNull null
            if (item.qty !in 1..MaxItemQty) return@mapNotNull null
            ParsedItem(name, item.unitPriceCents, item.qty)
        }
    }

    @Serializable
    private data class ModelReply(val items: List<ModelItem> = emptyList())

    @Serializable
    private data class ModelItem(
        val name: String = "",
        val unitPriceCents: Long = 0L,
        val qty: Int = 1,
    )

    private companion object {
        const val MaxImageEdge = 1536

        /** Sanity bounds — beyond these a line is a misreading, not an order. */
        const val MaxItemCents = 1_000_000L
        const val MaxItemQty = 99

        val Prompt = """
            You are reading a photo of a bar or restaurant tab.

            List only the items that were ordered. Reply with JSON and nothing
            else, in exactly this shape:
            {"items":[{"name":"Chopp","unitPriceCents":1200,"qty":2}]}

            Rules:
            - unitPriceCents is the price of ONE unit, in cents, as a whole number.
            - If a line shows the total for several units, divide it by the
              quantity to get the price of one.
            - qty is how many of that item were ordered.
            - Ignore totals, subtotals, service charges, tips, taxes, discounts,
              payment and change lines, table numbers, dates and venue details.
            - Copy each name as printed, tidied to ordinary capitalisation.
            - Never invent an item or a price. If a line is unreadable, leave it out.
        """.trimIndent()
    }
}
