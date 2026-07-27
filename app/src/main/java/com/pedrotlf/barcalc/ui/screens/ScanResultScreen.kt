package com.pedrotlf.barcalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrotlf.barcalc.R
import com.pedrotlf.barcalc.domain.SplitCalculator
import com.pedrotlf.barcalc.domain.receipt.ParsedItem
import com.pedrotlf.barcalc.domain.receipt.ReceiptParser
import com.pedrotlf.barcalc.ui.ScanResult
import com.pedrotlf.barcalc.ui.TabAction
import com.pedrotlf.barcalc.ui.components.LocalCurrencySymbol
import com.pedrotlf.barcalc.ui.components.PrimaryButton
import com.pedrotlf.barcalc.ui.theme.BarCalcTheme
import com.pedrotlf.barcalc.ui.theme.BarTabColors
import com.pedrotlf.barcalc.ui.theme.BarTabDimens
import com.pedrotlf.barcalc.ui.theme.BarTabType

/**
 * What the camera read off the tab, and what the parser made of it.
 *
 * Still read-only: nothing here reaches the tab yet, so the parse can be judged
 * against real tabs before an editing flow is built on top of it. The text the
 * parser worked from stays visible underneath, which is what tells a thin
 * result caused by a bad photo apart from one caused by a parsing rule.
 */
@Composable
fun ScanResultScreen(scanning: Boolean, result: ScanResult?, onAction: (TabAction) -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(BarTabColors.Bg),
    ) {
        ScreenScaffold(
            footer = {
                PrimaryButton(
                    text = stringResource(R.string.close),
                    onClick = { onAction(TabAction.DismissScanResult) },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        ) {
            ScreenHeader(
                title = stringResource(R.string.scan_result_title),
                subtitle = stringResource(R.string.scan_result_subtitle),
                leading = { HeaderCloseButton { onAction(TabAction.DismissScanResult) } },
            )

            when {
                scanning -> ScanningIndicator()
                result is ScanResult.Failed -> EmptyListHint(stringResource(R.string.scan_failed))
                result is ScanResult.Read && result.rawText.isBlank() ->
                    EmptyListHint(stringResource(R.string.scan_empty))
                result is ScanResult.Read -> {
                    if (result.items.isEmpty()) {
                        EmptyListHint(stringResource(R.string.scan_no_items))
                    } else {
                        ParsedItems(result.items)
                    }
                    // The text the parser worked from, so a thin result can be
                    // told apart from a bad photo at a glance.
                    RawTextSection(result.rawText)
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun ScanningIndicator() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = BarTabDimens.ScreenHPadding, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CircularProgressIndicator(
            color = BarTabColors.Accent500,
            modifier = Modifier.size(32.dp),
        )
        Text(
            stringResource(R.string.scan_reading),
            style = BarTabType.Hint,
            textAlign = TextAlign.Center,
        )
    }
}

/** What the parser made of the tab. Read-only for now — nothing is added yet. */
@Composable
private fun ParsedItems(items: List<ParsedItem>) {
    val currency = LocalCurrencySymbol.current
    Column(
        Modifier.padding(
            start = BarTabDimens.ScreenHPadding,
            top = 12.dp,
            end = BarTabDimens.ScreenHPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(BarTabDimens.ListGap),
    ) {
        items.forEach { item ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(BarTabDimens.RadiusLg))
                    .background(BarTabColors.Accent100)
                    .padding(horizontal = BarTabDimens.CardPadding, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(item.name, style = BarTabType.RowTitle, modifier = Modifier.weight(1f))
                if (item.qty > 1) {
                    Text("×${item.qty}", style = BarTabType.Caption)
                }
                Text(
                    SplitCalculator.formatMoney(item.priceCents * item.qty, currency),
                    style = BarTabType.Money,
                )
            }
        }
    }
}

/** The raw recognised text, kept for comparison against the parsed result. */
@Composable
private fun RawTextSection(text: String) {
    Column(
        Modifier.padding(
            start = BarTabDimens.ScreenHPadding,
            top = 16.dp,
            end = BarTabDimens.ScreenHPadding,
            bottom = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(stringResource(R.string.scan_raw_text_heading), style = BarTabType.Hint)
        Text(
            text,
            style = BarTabType.Body.copy(
                fontSize = 13.sp,
                color = BarTabColors.Neutral800,
                lineHeight = 19.sp,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(BarTabDimens.RadiusLg))
                .background(BarTabColors.Surface)
                .padding(BarTabDimens.CardPadding),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5EAD8, heightDp = 700)
@Composable
private fun ScanResultScreenPreview() {
    BarCalcTheme {
        val rawText = """
            BAR DO ZE
            2x Chopp        24,00
            BATATA FRITA    32,00
            Agua             6,00
            SUBTOTAL        62,00
        """.trimIndent()
        ScanResultScreen(
            scanning = false,
            result = ScanResult.Read(ReceiptParser.parse(rawText), rawText),
            onAction = {},
        )
    }
}

@Preview(name = "Reading", showBackground = true, backgroundColor = 0xFFF5EAD8, heightDp = 400)
@Composable
private fun ScanResultScreenLoadingPreview() {
    BarCalcTheme {
        ScanResultScreen(scanning = true, result = null, onAction = {})
    }
}
