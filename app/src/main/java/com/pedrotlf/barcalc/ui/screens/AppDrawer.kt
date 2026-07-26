package com.pedrotlf.barcalc.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrotlf.barcalc.R
import com.pedrotlf.barcalc.ui.TabAction
import com.pedrotlf.barcalc.ui.components.AppIcons
import com.pedrotlf.barcalc.ui.components.GhostIconButton
import com.pedrotlf.barcalc.ui.theme.BarCalcTheme
import com.pedrotlf.barcalc.ui.theme.BarTabColors
import com.pedrotlf.barcalc.ui.theme.BarTabDimens
import com.pedrotlf.barcalc.ui.theme.BarTabType

/**
 * Slide-in menu holding the app-level destinations (History, About). Opens
 * from the left, the same edge as the header button that toggles it.
 * Hand-rolled rather than Material's [androidx.compose.material3
 * .ModalNavigationDrawer] so it matches the app's own sheet styling.
 */
@Composable
fun AppDrawer(visible: Boolean, onAction: (TabAction) -> Unit) {
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(140)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(BarTabColors.Scrim)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onAction(TabAction.CloseDrawer) },
            )
        }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val panelWidth = (maxWidth * 0.72f).coerceAtMost(320.dp)
            AnimatedVisibility(
                visible = visible,
                modifier = Modifier.align(Alignment.CenterStart),
                enter = slideInHorizontally(tween(220)) { -it },
                exit = slideOutHorizontally(tween(180)) { -it },
            ) {
                Column(
                    Modifier
                        .width(panelWidth)
                        .fillMaxHeight()
                        .shadow(24.dp, RoundedCornerShape(topEnd = BarTabDimens.RadiusLg, bottomEnd = BarTabDimens.RadiusLg))
                        .clip(RoundedCornerShape(topEnd = BarTabDimens.RadiusLg, bottomEnd = BarTabDimens.RadiusLg))
                        .background(BarTabColors.Bg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { /* swallow clicks so the scrim doesn't close */ },
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, top = 16.dp, end = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.menu_title),
                            style = BarTabType.ScreenTitle.copy(fontSize = 20.sp),
                            modifier = Modifier.weight(1f),
                        )
                        GhostIconButton(
                            icon = AppIcons.Close,
                            contentDescription = stringResource(R.string.cd_close),
                            onClick = { onAction(TabAction.CloseDrawer) },
                            size = 36.dp,
                            iconSize = 16.dp,
                        )
                    }
                    HorizontalDivider(color = BarTabColors.Accent200, thickness = 1.dp)

                    DrawerItem(
                        icon = AppIcons.History,
                        label = stringResource(R.string.menu_history),
                        onClick = { onAction(TabAction.ShowHistory) },
                    )
                    DrawerItem(
                        icon = AppIcons.Help,
                        label = stringResource(R.string.menu_about),
                        onClick = { onAction(TabAction.ShowAbout) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icon, contentDescription = null, Modifier.size(20.dp), tint = BarTabColors.Accent600)
        Text(label, style = BarTabType.RowTitle.copy(fontSize = 15.sp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5EAD8, heightDp = 600)
@Composable
private fun AppDrawerPreview() {
    BarCalcTheme {
        AppDrawer(visible = true, onAction = {})
    }
}
