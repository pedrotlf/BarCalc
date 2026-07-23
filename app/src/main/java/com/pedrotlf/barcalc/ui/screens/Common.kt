package com.pedrotlf.barcalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pedrotlf.barcalc.ui.components.topBorder
import com.pedrotlf.barcalc.ui.theme.BarTabColors
import com.pedrotlf.barcalc.ui.theme.BarTabDimens
import com.pedrotlf.barcalc.ui.theme.BarTabType

/**
 * Title + subtitle block at the top of every screen, with an optional
 * trailing [action] (e.g. an info button) pinned to the top-right.
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    action: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(
                start = BarTabDimens.ScreenHPadding,
                top = 24.dp,
                end = BarTabDimens.ScreenHPadding,
                bottom = 4.dp,
            ),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = BarTabType.ScreenTitle)
            Text(subtitle, style = BarTabType.ScreenSubtitle, modifier = Modifier.padding(top = 4.dp))
        }
        action?.invoke(this)
    }
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
