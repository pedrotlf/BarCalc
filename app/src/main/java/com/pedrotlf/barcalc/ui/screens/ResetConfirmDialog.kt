package com.pedrotlf.barcalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrotlf.barcalc.R
import com.pedrotlf.barcalc.ui.TabAction
import com.pedrotlf.barcalc.ui.components.PrimaryButton
import com.pedrotlf.barcalc.ui.components.SecondaryButton
import com.pedrotlf.barcalc.ui.theme.BarCalcTheme
import com.pedrotlf.barcalc.ui.theme.BarTabColors
import com.pedrotlf.barcalc.ui.theme.BarTabDimens
import com.pedrotlf.barcalc.ui.theme.BarTabType

/**
 * Centered confirmation before wiping the tab to start over — guards against an
 * accidental tap on the results "Done" button. Scrim tap or Cancel dismisses;
 * only [TabAction.Reset] actually clears the session.
 */
@Composable
fun ResetConfirmDialog(onAction: (TabAction) -> Unit) {
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(BarTabColors.Scrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onAction(TabAction.DismissReset) }
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        val dialogWidth = (maxWidth * BarTabDimens.SheetWidthFraction).coerceAtMost(360.dp)
        Column(
            Modifier
                .width(dialogWidth)
                .shadow(24.dp, RoundedCornerShape(BarTabDimens.RadiusLg))
                .clip(RoundedCornerShape(BarTabDimens.RadiusLg))
                .background(BarTabColors.Bg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { /* swallow clicks so the scrim doesn't close */ }
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                stringResource(R.string.reset_confirm_title),
                style = BarTabType.RowTitle.copy(fontSize = 17.sp),
            )
            Text(
                stringResource(R.string.reset_confirm_message),
                style = BarTabType.Body.copy(
                    fontSize = 13.sp,
                    color = BarTabColors.Neutral700,
                    lineHeight = 19.sp,
                ),
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SecondaryButton(
                    text = stringResource(R.string.cancel),
                    onClick = { onAction(TabAction.DismissReset) },
                    modifier = Modifier.weight(1f),
                )
                PrimaryButton(
                    text = stringResource(R.string.reset_confirm_confirm),
                    onClick = { onAction(TabAction.Reset) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFF5EAD8)
@Composable
private fun ResetConfirmDialogPreview() {
    BarCalcTheme {
        ResetConfirmDialog(onAction = {})
    }
}
