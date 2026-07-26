package com.pedrotlf.barcalc.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.pedrotlf.barcalc.R
import com.pedrotlf.barcalc.ui.TabAction
import com.pedrotlf.barcalc.ui.theme.BarCalcTheme

/**
 * Confirmation before finishing the tab — it's archived to history and the
 * working tab is cleared. Guards against an accidental tap on results "Done".
 */
@Composable
fun ResetConfirmDialog(onAction: (TabAction) -> Unit) {
    ConfirmDialog(
        title = stringResource(R.string.reset_confirm_title),
        message = stringResource(R.string.reset_confirm_message),
        confirmText = stringResource(R.string.reset_confirm_confirm),
        onConfirm = { onAction(TabAction.Reset) },
        onDismiss = { onAction(TabAction.DismissReset) },
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF5EAD8)
@Composable
private fun ResetConfirmDialogPreview() {
    BarCalcTheme {
        ResetConfirmDialog(onAction = {})
    }
}
