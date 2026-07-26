package com.pedrotlf.barcalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import com.pedrotlf.barcalc.ui.components.PrimaryButton
import com.pedrotlf.barcalc.ui.components.SecondaryButton
import com.pedrotlf.barcalc.ui.theme.BarTabColors
import com.pedrotlf.barcalc.ui.theme.BarTabDimens
import com.pedrotlf.barcalc.ui.theme.BarTabType

/**
 * Shared centered confirmation modal: scrim + card, Cancel alongside a single
 * confirming action. Scrim tap and Cancel both call [onDismiss].
 *
 * [extraContent] lets a caller slot something between the message and the
 * buttons (the rename prompt puts its text field there).
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String?,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean = true,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(BarTabColors.Scrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onDismiss() }
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
            Text(title, style = BarTabType.RowTitle.copy(fontSize = 17.sp))
            if (message != null) {
                Text(
                    message,
                    style = BarTabType.Body.copy(
                        fontSize = 13.sp,
                        color = BarTabColors.Neutral700,
                        lineHeight = 19.sp,
                    ),
                )
            }
            extraContent?.invoke(this)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SecondaryButton(
                    text = stringResource(R.string.cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                PrimaryButton(
                    text = confirmText,
                    onClick = onConfirm,
                    enabled = confirmEnabled,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
