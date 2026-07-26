package com.pedrotlf.barcalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.pedrotlf.barcalc.domain.HistoryEntry
import com.pedrotlf.barcalc.domain.SplitCalculator
import com.pedrotlf.barcalc.ui.TabAction
import com.pedrotlf.barcalc.ui.components.AppIcons
import com.pedrotlf.barcalc.ui.components.GhostIconButton
import com.pedrotlf.barcalc.ui.components.LocalCurrencySymbol
import com.pedrotlf.barcalc.ui.components.PrimaryButton
import com.pedrotlf.barcalc.ui.theme.BarCalcTheme
import com.pedrotlf.barcalc.ui.theme.BarTabColors
import com.pedrotlf.barcalc.ui.theme.BarTabDimens
import com.pedrotlf.barcalc.ui.theme.BarTabType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Full-screen overlay listing archived tabs, newest first. Tapping an entry
 * starts a new tab from it; the archived copy is never modified.
 */
@Composable
fun HistoryScreen(entries: List<HistoryEntry>, onAction: (TabAction) -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(BarTabColors.Bg),
    ) {
        ScreenScaffold(
            footer = {
                PrimaryButton(
                    text = stringResource(R.string.close),
                    onClick = { onAction(TabAction.HideHistory) },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        ) {
            ScreenHeader(
                stringResource(R.string.history_title),
                stringResource(R.string.history_subtitle),
            ) {
                GhostIconButton(
                    icon = AppIcons.Close,
                    contentDescription = stringResource(R.string.cd_close),
                    onClick = { onAction(TabAction.HideHistory) },
                    size = 40.dp,
                    iconSize = 18.dp,
                    tint = BarTabColors.Neutral600,
                )
            }

            if (entries.isEmpty()) {
                EmptyListHint(stringResource(R.string.history_empty))
            } else {
                Text(
                    stringResource(R.string.history_duplicate_hint),
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
                        bottom = 8.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(BarTabDimens.ListGap),
                ) {
                    entries.forEach { entry ->
                        HistoryRow(entry, onAction)
                    }
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = BarTabDimens.ScreenHPadding,
                            end = BarTabDimens.ScreenHPadding,
                            bottom = 16.dp,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(R.string.history_clear_all),
                        style = BarTabType.Caption.copy(color = BarTabColors.Accent700),
                        modifier = Modifier
                            .clip(RoundedCornerShape(BarTabDimens.RadiusSm))
                            .clickable { onAction(TabAction.RequestClearHistory) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry, onAction: (TabAction) -> Unit) {
    val currency = LocalCurrencySymbol.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BarTabDimens.RadiusLg))
            .background(BarTabColors.Surface)
            .clickable { onAction(TabAction.RequestDuplicate(entry.id)) }
            .padding(horizontal = BarTabDimens.CardPadding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                entry.customName?.takeIf { it.isNotBlank() } ?: formatSavedAt(entry.savedAt),
                style = BarTabType.RowTitle.copy(fontSize = 15.sp),
            )
            Text(
                stringResource(R.string.history_entry_summary, entry.itemCount, entry.personCount),
                style = BarTabType.Caption,
            )
            // A renamed entry would otherwise lose its date, so keep it visible.
            if (entry.hasCustomName) {
                Text(formatSavedAt(entry.savedAt), style = BarTabType.Caption)
            }
        }
        Text(
            SplitCalculator.formatMoney(entry.totalCents, currency),
            style = BarTabType.Money,
        )
        GhostIconButton(
            icon = AppIcons.Pencil,
            contentDescription = stringResource(R.string.cd_rename_entry),
            onClick = { onAction(TabAction.RequestRename(entry.id)) },
            size = 32.dp,
            iconSize = 15.dp,
            tint = BarTabColors.Neutral600,
        )
        GhostIconButton(
            icon = AppIcons.Trash,
            contentDescription = stringResource(R.string.cd_delete_entry),
            onClick = { onAction(TabAction.RequestDeleteEntry(entry.id)) },
            size = 32.dp,
            iconSize = 15.dp,
            tint = BarTabColors.Neutral600,
        )
    }
}

/** Localized "23 Jul 2026, 21:14" — follows the app's locale automatically. */
private fun formatSavedAt(millis: Long): String =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT))

@Preview(showBackground = true, backgroundColor = 0xFFF5EAD8, heightDp = 700)
@Composable
private fun HistoryScreenPreview() {
    BarCalcTheme {
        HistoryScreen(
            entries = listOf(
                HistoryEntry(1, System.currentTimeMillis(), null, 6, 3, 12_040L),
                HistoryEntry(2, System.currentTimeMillis() - 86_400_000L, "Sexta no Zé", 4, 2, 8_600L),
            ),
            onAction = {},
        )
    }
}
