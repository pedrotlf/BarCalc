package com.pedrotlf.barcalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pedrotlf.barcalc.R
import com.pedrotlf.barcalc.ui.components.AppIcons
import com.pedrotlf.barcalc.ui.components.GhostIconButton
import com.pedrotlf.barcalc.ui.components.topBorder
import com.pedrotlf.barcalc.ui.theme.BarTabColors
import com.pedrotlf.barcalc.ui.theme.BarTabDimens
import com.pedrotlf.barcalc.ui.theme.BarTabType

/**
 * Title + subtitle block at the top of every screen.
 *
 * [leading] is the navigation slot, and always occupies its space so the
 * title starts at the same x on every screen and never jumps when
 * navigating. Following the usual top-bar convention, it holds whichever
 * control that screen needs: the menu on the root screen, a back arrow
 * deeper in the wizard, an X on screens that dismiss.
 *
 * [action] is an optional screen-level shortcut on the trailing edge. It is
 * only laid out when present — sitting after the flexible title column, so
 * omitting it just gives long subtitles more room rather than moving the
 * title.
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    leading: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(
                start = BarTabDimens.HeaderHPadding,
                top = 16.dp,
                end = BarTabDimens.HeaderHPadding,
                bottom = 4.dp,
            ),
        verticalAlignment = Alignment.Top,
    ) {
        HeaderSlot(leading)
        // Nudged down so the title's first line centres on the slot button:
        // the slot's centre is 24dp down (half of HeaderSlot), the title's is
        // ~16dp (half its line box), so 8dp of padding lines them up. The row's
        // own top padding absorbs it, leaving the title's final y unchanged.
        Column(
            Modifier
                .weight(1f)
                .padding(top = 8.dp),
        ) {
            Text(title, style = BarTabType.ScreenTitle)
            Text(subtitle, style = BarTabType.ScreenSubtitle, modifier = Modifier.padding(top = 4.dp))
        }
        action?.invoke()
    }
}

/** Fixed-size header gutter; renders [content] centred, or nothing at all. */
@Composable
private fun HeaderSlot(content: (@Composable () -> Unit)?) {
    Box(
        Modifier.size(BarTabDimens.HeaderSlot),
        contentAlignment = Alignment.Center,
    ) {
        content?.invoke()
    }
}

/** Drawer toggle, for the root screen that has nothing to go back to. */
@Composable
fun HeaderMenuButton(onClick: () -> Unit) {
    GhostIconButton(
        icon = AppIcons.Menu,
        contentDescription = stringResource(R.string.cd_menu),
        onClick = onClick,
        size = BarTabDimens.HeaderSlot,
        iconSize = BarTabDimens.HeaderIcon,
        tint = BarTabColors.Neutral700,
    )
}

/** Shortcut straight to the history screen, sharing the nav slot styling. */
@Composable
fun HeaderHistoryButton(onClick: () -> Unit) {
    GhostIconButton(
        icon = AppIcons.History,
        contentDescription = stringResource(R.string.menu_history),
        onClick = onClick,
        size = BarTabDimens.HeaderSlot,
        iconSize = BarTabDimens.HeaderIcon,
        tint = BarTabColors.Neutral700,
    )
}

/** Back arrow for screens that step backwards through the wizard. */
@Composable
fun HeaderBackButton(onClick: () -> Unit) {
    GhostIconButton(
        icon = AppIcons.ArrowLeft,
        contentDescription = stringResource(R.string.cd_back),
        onClick = onClick,
        size = BarTabDimens.HeaderSlot,
        iconSize = BarTabDimens.HeaderIcon,
        tint = BarTabColors.Neutral700,
    )
}

/** Close (X) for screens that dismiss rather than step back. */
@Composable
fun HeaderCloseButton(onClick: () -> Unit) {
    GhostIconButton(
        icon = AppIcons.Close,
        contentDescription = stringResource(R.string.cd_close),
        onClick = onClick,
        size = BarTabDimens.HeaderSlot,
        iconSize = BarTabDimens.HeaderIcon,
        tint = BarTabColors.Neutral700,
    )
}

/**
 * Screen shell: scrollable content over a pinned footer with a hairline
 * top border, like the design's sticky bottom bars.
 */
@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    // With the keyboard up (especially in landscape) a pinned footer would eat
    // most of the little height left, so it joins the scroll. It still sticks to
    // the bottom while the content is short (SpaceBetween over a min-height column)
    // and only scrolls once the content is tall.
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val scrollState = rememberScrollState()
    Column(modifier.fillMaxSize()) {
        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .imePadding(),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = if (imeVisible) maxHeight else 0.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = if (imeVisible) Arrangement.SpaceBetween else Arrangement.Top,
            ) {
                Column(Modifier.fillMaxWidth(), content = content)
                if (imeVisible) FooterBar(footer)
            }
        }
        if (!imeVisible) FooterBar(footer)
    }
}

/** The pinned/inline bottom bar: hairline top border, sticky-bottom styling. */
@Composable
private fun FooterBar(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(BarTabColors.Bg)
            .topBorder(BarTabColors.Accent200)
            .padding(
                start = BarTabDimens.ScreenHPadding,
                top = 14.dp,
                end = BarTabDimens.ScreenHPadding,
                bottom = BarTabDimens.ScreenHPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(BarTabDimens.ListGap),
        content = content,
    )
}

/** Hairline rule between sections of a sheet or menu. */
@Composable
fun SectionDivider() {
    HorizontalDivider(color = BarTabColors.Surface, thickness = 1.dp)
}

/** Centered muted hint shown when a list has no entries yet. */
@Composable
fun EmptyListHint(text: String) {
    Text(
        text,
        style = BarTabType.EmptyHint,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = BarTabDimens.ScreenHPadding, vertical = 36.dp),
    )
}
