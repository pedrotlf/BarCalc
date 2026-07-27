package com.pedrotlf.barcalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrotlf.barcalc.R
import com.pedrotlf.barcalc.domain.SplitCalculator
import com.pedrotlf.barcalc.domain.receipt.ReceiptParser
import com.pedrotlf.barcalc.ui.ScanDraft
import com.pedrotlf.barcalc.ui.ScanResult
import com.pedrotlf.barcalc.ui.TabAction
import com.pedrotlf.barcalc.ui.components.AppIcons
import com.pedrotlf.barcalc.ui.components.BareTextField
import com.pedrotlf.barcalc.ui.components.GhostIconButton
import com.pedrotlf.barcalc.ui.components.LocalCurrencySymbol
import com.pedrotlf.barcalc.ui.components.MoneyField
import com.pedrotlf.barcalc.ui.components.NameCapitalization
import com.pedrotlf.barcalc.ui.components.PrimaryButton
import com.pedrotlf.barcalc.ui.components.QtyStepper
import com.pedrotlf.barcalc.ui.components.StepperSize
import com.pedrotlf.barcalc.ui.components.accentCheckboxColors
import com.pedrotlf.barcalc.ui.theme.BarCalcTheme
import com.pedrotlf.barcalc.ui.theme.BarTabColors
import com.pedrotlf.barcalc.ui.theme.BarTabDimens
import com.pedrotlf.barcalc.ui.theme.BarTabType

/**
 * Check over what was scanned before any of it joins the tab.
 *
 * Scanning misreads things, so nothing here is taken on trust: every line can
 * be corrected or dropped, and the running total is shown so it can be held up
 * against the total printed on the tab — the quickest way to catch a price that
 * came back wrong.
 */
@Composable
fun ScanReviewScreen(scanning: Boolean, result: ScanResult?, onAction: (TabAction) -> Unit) {
    val currency = LocalCurrencySymbol.current
    val read = result as? ScanResult.Read

    Box(
        Modifier
            .fillMaxSize()
            .background(BarTabColors.Bg),
    ) {
        ScreenScaffold(
            footer = {
                if (read != null && read.drafts.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            pluralStringResource(
                                R.plurals.scan_selected_count,
                                read.included.size,
                                read.included.size,
                            ),
                            style = BarTabType.LabelMuted,
                        )
                        Text(
                            SplitCalculator.formatMoney(read.includedTotalCents, currency),
                            style = BarTabType.Label.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
                PrimaryButton(
                    text = stringResource(R.string.scan_add_items),
                    onClick = { onAction(TabAction.ConfirmScannedItems) },
                    enabled = read != null && read.included.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        ) {
            ScreenHeader(
                title = stringResource(R.string.scan_review_title),
                subtitle = stringResource(R.string.scan_review_subtitle),
                leading = { HeaderCloseButton { onAction(TabAction.DismissScanResult) } },
            )

            when {
                scanning -> ScanningIndicator()
                result is ScanResult.Failed -> EmptyListHint(stringResource(R.string.scan_failed))
                read != null && read.rawText.isBlank() ->
                    EmptyListHint(stringResource(R.string.scan_empty))
                read != null -> {
                    if (read.drafts.isEmpty()) {
                        EmptyListHint(stringResource(R.string.scan_no_items))
                    } else {
                        Text(
                            stringResource(R.string.scan_review_warning),
                            style = BarTabType.Hint,
                            modifier = Modifier.padding(
                                start = BarTabDimens.ScreenHPadding,
                                end = BarTabDimens.ScreenHPadding,
                                top = 8.dp,
                            ),
                        )
                        Column(
                            Modifier.padding(
                                start = BarTabDimens.ScreenHPadding,
                                top = 12.dp,
                                end = BarTabDimens.ScreenHPadding,
                            ),
                            verticalArrangement = Arrangement.spacedBy(BarTabDimens.ListGap),
                        ) {
                            read.drafts.forEach { draft ->
                                key(draft.id) { DraftRow(draft, onAction) }
                            }
                        }
                    }
                    // What the parser worked from, so a line it missed can be
                    // spotted and typed in rather than silently lost.
                    RawTextSection(read.rawText)
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

/**
 * One scanned line, laid out like the tab's own item rows so correcting a
 * scan feels the same as editing an item. Dropped lines stay visible but
 * dimmed, so nothing disappears without the user doing it.
 */
@Composable
private fun DraftRow(draft: ScanDraft, onAction: (TabAction) -> Unit) {
    val currency = LocalCurrencySymbol.current
    val priceStyle = BarTabType.Caption.copy(color = BarTabColors.Neutral700)

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BarTabDimens.RadiusLg))
            .background(BarTabColors.Accent100)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Checkbox(
            checked = draft.included,
            onCheckedChange = { onAction(TabAction.ToggleScanDraft(draft.id)) },
            colors = accentCheckboxColors(),
        )
        FlowRow(
            modifier = Modifier
                .weight(1f)
                .alpha(if (draft.included) 1f else 0.45f),
            horizontalArrangement = Arrangement.End,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            BareTextField(
                value = draft.name,
                onValueChange = { onAction(TabAction.ScanDraftNameChanged(draft.id, it)) },
                textStyle = BarTabType.RowTitle,
                keyboardOptions = NameCapitalization,
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = 96.dp)
                    .padding(end = 6.dp),
            )
            Row(
                Modifier
                    .padding(end = 6.dp)
                    .clip(RoundedCornerShape(BarTabDimens.RadiusSm))
                    .background(BarTabColors.Bg)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(currency, style = BarTabType.Caption)
                MoneyField(
                    cents = draft.priceCents,
                    onCentsChange = { onAction(TabAction.ScanDraftPriceChanged(draft.id, it)) },
                    textStyle = priceStyle,
                    modifier = Modifier.widthIn(min = 44.dp),
                )
            }
            QtyStepper(
                label = "${draft.qty}",
                onDec = { onAction(TabAction.DecScanDraftQty(draft.id)) },
                onInc = { onAction(TabAction.IncScanDraftQty(draft.id)) },
                size = StepperSize.Compact,
            )
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

private val PreviewRawText = """
    BAR DO ZE
    2x Chopp        24,00
    BATATA FRITA    32,00
    Agua             6,00
    SUBTOTAL        62,00
""".trimIndent()

@Preview(showBackground = true, backgroundColor = 0xFFF5EAD8, heightDp = 760)
@Composable
private fun ScanReviewScreenPreview() {
    BarCalcTheme {
        val drafts = ReceiptParser.parse(PreviewRawText)
            .mapIndexed { index, item -> ScanDraft.from(index, item) }
        ScanReviewScreen(
            scanning = false,
            result = ScanResult.Read(drafts, PreviewRawText),
            onAction = {},
        )
    }
}

@Preview(name = "One line dropped", showBackground = true, backgroundColor = 0xFFF5EAD8, heightDp = 760)
@Composable
private fun ScanReviewScreenPartialPreview() {
    BarCalcTheme {
        val drafts = ReceiptParser.parse(PreviewRawText)
            .mapIndexed { index, item -> ScanDraft.from(index, item) }
            .map { if (it.id == 1) it.copy(included = false) else it }
        ScanReviewScreen(
            scanning = false,
            result = ScanResult.Read(drafts, PreviewRawText),
            onAction = {},
        )
    }
}

@Preview(name = "Reading", showBackground = true, backgroundColor = 0xFFF5EAD8, heightDp = 400)
@Composable
private fun ScanReviewScreenLoadingPreview() {
    BarCalcTheme {
        ScanReviewScreen(scanning = true, result = null, onAction = {})
    }
}
