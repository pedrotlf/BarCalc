package com.pedrotlf.barcalc.data.receipt

import com.pedrotlf.barcalc.domain.receipt.ParsedItem
import com.pedrotlf.barcalc.domain.receipt.ReceiptParser

/** Which reader produced a result — shown while scanning is still being judged. */
enum class ReadingSource { MODEL, TEXT }

/**
 * What a reader made of a photographed tab. [rawText] is whatever the reader
 * worked from or produced, kept so a thin result can be held up against it.
 */
data class TabReading(
    val items: List<ParsedItem>,
    val rawText: String,
    val source: ReadingSource,
)

/** Turns a photo of a tab into items, however it manages that. */
interface TabReader {
    suspend fun read(imageUri: String): Result<TabReading>
}

/**
 * Text recognition followed by the line parser — the reading that works
 * everywhere, since it needs nothing of the device beyond the bundled model.
 */
class TextTabReader(private val recognizer: ReceiptTextRecognizer) : TabReader {

    override suspend fun read(imageUri: String): Result<TabReading> =
        recognizer.recognize(imageUri).map { text ->
            TabReading(ReceiptParser.parse(text), text, ReadingSource.TEXT)
        }
}

/**
 * Tries each reader in turn, taking the first that comes back with anything.
 *
 * A reader that isn't available on this device, fails, or simply finds no
 * items hands over to the next, so the better reading is used where it can be
 * and the dependable one still covers everywhere else.
 */
class ChainedTabReader(private val readers: List<TabReader>) : TabReader {

    override suspend fun read(imageUri: String): Result<TabReading> {
        var lastFailure: Result<TabReading>? = null
        readers.forEach { reader ->
            val result = reader.read(imageUri)
            val reading = result.getOrNull()
            if (reading != null && reading.items.isNotEmpty()) return result
            if (result.isFailure) lastFailure = result
        }
        // Nothing found anything. A failure is more informative than an empty
        // reading, so report that if one happened.
        return lastFailure ?: Result.success(TabReading(emptyList(), "", ReadingSource.TEXT))
    }
}
