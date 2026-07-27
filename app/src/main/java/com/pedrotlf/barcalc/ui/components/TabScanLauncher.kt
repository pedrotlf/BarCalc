package com.pedrotlf.barcalc.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.pedrotlf.barcalc.ui.TabAction

/**
 * Capture settings for photographing a tab: one page, straight to the camera,
 * and the full editor so the crop, deskew and clean-up steps run before we
 * ever look at the image — that preprocessing is most of what makes text on a
 * curled till receipt readable.
 */
private val ScannerOptions = GmsDocumentScannerOptions.Builder()
    .setPageLimit(1)
    .setGalleryImportAllowed(false)
    .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
    .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE_WITH_FILTER)
    .build()

/**
 * Returns a function that opens the tab scanner, reporting the captured photo
 * back through [onAction].
 *
 * The scanner UI belongs to Google Play services and runs in its own process,
 * which is why the app itself never asks for the camera permission.
 */
@Composable
fun rememberTabScanLauncher(onAction: (TabAction) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        val page = GmsDocumentScanningResult
            .fromActivityResultIntent(result.data)
            ?.pages
            ?.firstOrNull()
            ?.imageUri
        if (result.resultCode == Activity.RESULT_OK && page != null) {
            onAction(TabAction.ReceiptCaptured(page.toString()))
        } else {
            // Backing out of the scanner is ordinary, not an error.
            onAction(TabAction.ScanCancelled)
        }
    }

    val client = remember { GmsDocumentScanning.getClient(ScannerOptions) }
    return {
        val activity = context.findActivity()
        if (activity == null) {
            onAction(TabAction.ScanCancelled)
        } else {
            client.getStartScanIntent(activity)
                .addOnSuccessListener { intentSender ->
                    launcher.launch(IntentSenderRequest.Builder(intentSender).build())
                }
                // Play services too old, or the module can't be fetched.
                .addOnFailureListener { onAction(TabAction.ScanCancelled) }
        }
    }
}

/** Compose hands out a wrapped context; the scanner needs the Activity itself. */
private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
