package com.pedrotlf.barcalc.data.receipt

import android.content.Context
import androidx.core.net.toUri
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Reads the text off a photo of a tab.
 *
 * An interface so [com.pedrotlf.barcalc.ui.TabViewModel] can be tested with a
 * fake — ML Kit needs an Android runtime, and later phases will want to feed
 * the parser known text without going near a camera.
 */
interface ReceiptTextRecognizer {

    /**
     * All text found in the image at [imageUri], or the failure that stopped us
     * reading it. Takes the uri as a string so the seam stays free of Android
     * types and can be faked in plain unit tests.
     */
    suspend fun recognize(imageUri: String): Result<String>
}

/**
 * ML Kit's on-device recognizer, using the Latin-script model bundled into the
 * app: nothing is uploaded and no model is fetched, so scanning works offline
 * and on first launch.
 */
class MlKitReceiptTextRecognizer(private val context: Context) : ReceiptTextRecognizer {

    private val client by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    override suspend fun recognize(imageUri: String): Result<String> = runCatching {
        val image = InputImage.fromFilePath(context, imageUri.toUri())
        client.process(image).await().text
    }
}

/**
 * Bridges a Play Services [Task] to a coroutine. Hand-rolled rather than
 * pulling in kotlinx-coroutines-play-services for this one call.
 */
private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { error -> continuation.resumeWithException(error) }
    addOnCanceledListener { continuation.cancel() }
}
