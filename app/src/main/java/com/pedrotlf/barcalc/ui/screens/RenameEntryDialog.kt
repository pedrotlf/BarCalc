package com.pedrotlf.barcalc.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pedrotlf.barcalc.R
import com.pedrotlf.barcalc.ui.TabAction
import com.pedrotlf.barcalc.ui.components.NameCapitalization
import com.pedrotlf.barcalc.ui.components.PillTextField
import com.pedrotlf.barcalc.ui.theme.BarCalcTheme

/**
 * Prompt for a history entry's custom name. Clearing the field and saving
 * restores the generated date summary.
 */
@Composable
fun RenameEntryDialog(draft: String, onAction: (TabAction) -> Unit) {
    ConfirmDialog(
        title = stringResource(R.string.history_rename_title),
        message = null,
        confirmText = stringResource(R.string.history_rename_confirm),
        onConfirm = { onAction(TabAction.ConfirmRename) },
        onDismiss = { onAction(TabAction.DismissRename) },
    ) {
        PillTextField(
            value = draft,
            onValueChange = { onAction(TabAction.RenameDraftChanged(it)) },
            placeholder = stringResource(R.string.history_rename_placeholder),
            keyboardOptions = NameCapitalization,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5EAD8)
@Composable
private fun RenameEntryDialogPreview() {
    BarCalcTheme {
        RenameEntryDialog(draft = "Sexta no Zé", onAction = {})
    }
}
