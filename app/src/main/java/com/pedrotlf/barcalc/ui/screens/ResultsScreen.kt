package com.pedrotlf.barcalc.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrotlf.barcalc.R
import com.pedrotlf.barcalc.domain.Person
import com.pedrotlf.barcalc.domain.SplitCalculator
import com.pedrotlf.barcalc.ui.Screen
import com.pedrotlf.barcalc.ui.TabAction
import com.pedrotlf.barcalc.ui.TabUiState
import com.pedrotlf.barcalc.ui.components.AppIcons
import com.pedrotlf.barcalc.ui.components.Avatar
import com.pedrotlf.barcalc.ui.components.LocalCurrencySymbol
import com.pedrotlf.barcalc.ui.components.PrimaryButton
import com.pedrotlf.barcalc.ui.components.topBorder
import com.pedrotlf.barcalc.ui.previewTabState
import com.pedrotlf.barcalc.ui.theme.BarCalcTheme
import com.pedrotlf.barcalc.ui.theme.BarTabColors
import com.pedrotlf.barcalc.ui.theme.BarTabDimens
import com.pedrotlf.barcalc.ui.theme.BarTabType

/** Screen 3 — the final split, one expandable card per person. */
@Composable
fun ResultsScreen(state: TabUiState, onAction: (TabAction) -> Unit) {
    val currency = LocalCurrencySymbol.current
    val subtotal = SplitCalculator.subtotal(state.items)
    val tipAmount = SplitCalculator.tipAmount(subtotal, state.tipEnabled, state.tipPercent)
    val peopleLabel = pluralStringResource(
        R.plurals.people_count,
        state.people.size,
        state.people.size,
    )

    ScreenScaffold(
        footer = {
            PrimaryButton(
                text = stringResource(R.string.done),
                onClick = { onAction(TabAction.Reset) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        ScreenHeader(
            stringResource(R.string.results_title),
            stringResource(
                R.string.results_subtitle,
                peopleLabel,
                SplitCalculator.formatMoney(subtotal + tipAmount, currency),
            ),
        )
        Column(
            Modifier.padding(
                start = BarTabDimens.ScreenHPadding,
                top = 16.dp,
                end = BarTabDimens.ScreenHPadding,
                bottom = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(BarTabDimens.ListGap),
        ) {
            state.people.forEachIndexed { index, person ->
                ResultCard(person, index, state, onAction, subtotal, tipAmount)
            }
        }
    }
}

@Composable
private fun ResultCard(
    person: Person,
    index: Int,
    state: TabUiState,
    onAction: (TabAction) -> Unit,
    subtotalCents: Long,
    tipCents: Long,
) {
    val currency = LocalCurrencySymbol.current
    val itemsTotal = SplitCalculator.personItemsTotal(state.items, person.id)
    val tipShare = SplitCalculator.personTipShare(
        personIndex = index,
        peopleCount = state.people.size,
        tipCents = tipCents,
        personItemsCents = itemsTotal,
        subtotalCents = subtotalCents,
    )
    val expanded = person.id in state.expandedResultIds
    val chevronDeg by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(150),
        label = "chevron",
    )

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BarTabDimens.RadiusXl))
            .background(BarTabColors.Surface)
            .padding(horizontal = BarTabDimens.CardPadding, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { onAction(TabAction.ToggleExpand(person.id)) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Avatar(SplitCalculator.initialsFor(person.name), BarTabColors.avatarColor(index))
            Text(
                person.name,
                style = BarTabType.RowTitle.copy(fontSize = 15.sp),
                modifier = Modifier.weight(1f),
            )
            Text(
                SplitCalculator.formatMoney(itemsTotal + tipShare, currency),
                style = BarTabType.Money.copy(fontSize = 16.sp),
            )
            Icon(
                AppIcons.ChevronRight,
                contentDescription = stringResource(
                    if (expanded) R.string.cd_collapse else R.string.cd_expand,
                ),
                tint = BarTabColors.Neutral500,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(chevronDeg),
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .topBorder(BarTabColors.Accent200)
                    .padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                state.items.forEach { item ->
                    val count = SplitCalculator.personUnitCount(item, person.id)
                    if (count == 0) return@forEach
                    val amount = SplitCalculator.personItemCost(item, person.id)
                    val shared = item.units.any { person.id in it && it.size > 1 }
                    val lineLabel = stringResource(
                        if (shared) R.string.result_line_item_shared else R.string.result_line_item,
                        item.name,
                        count,
                    )
                    BreakdownLine(
                        label = lineLabel,
                        amount = SplitCalculator.formatMoney(amount, currency),
                    )
                }
                if (state.tipEnabled) {
                    BreakdownLine(
                        stringResource(R.string.tip_share),
                        SplitCalculator.formatMoney(tipShare, currency),
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakdownLine(label: String, amount: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        val style = BarTabType.Body.copy(fontSize = 13.sp, color = BarTabColors.Neutral700)
        Text(label, style = style)
        Text(amount, style = style)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5EAD8, heightDp = 700)
@Composable
private fun ResultsScreenPreview() {
    BarCalcTheme {
        ResultsScreen(
            state = previewTabState(Screen.RESULTS).copy(expandedResultIds = setOf(1)),
            onAction = {},
        )
    }
}
