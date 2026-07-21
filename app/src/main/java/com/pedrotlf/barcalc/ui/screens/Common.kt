package com.pedrotlf.barcalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pedrotlf.barcalc.ui.components.topBorder
import com.pedrotlf.barcalc.ui.theme.BarTabColors
import com.pedrotlf.barcalc.ui.theme.BarTabDimens
import com.pedrotlf.barcalc.ui.theme.BarTabType

/** Title + subtitle block at the top of every screen. */
@Composable
fun ScreenHeader(title: String, subtitle: String) {
    Column(
        Modifier.padding(
            start = BarTabDimens.ScreenHPadding,
            top = 24.dp,
            end = BarTabDimens.ScreenHPadding,
            bottom = 4.dp,
        )
    ) {
        Text(title, style = BarTabType.ScreenTitle)
        Text(subtitle, style = BarTabType.ScreenSubtitle, modifier = Modifier.padding(top = 4.dp))
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
    Column(modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            content = content,
        )
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
            content = footer,
        )
    }
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
