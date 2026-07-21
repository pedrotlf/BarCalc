package com.pedrotlf.barcalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
import com.pedrotlf.barcalc.ui.components.GhostIconButton
import com.pedrotlf.barcalc.ui.components.LocalCurrencySymbol
import com.pedrotlf.barcalc.ui.components.PillTextField
import com.pedrotlf.barcalc.ui.components.PrimaryButton
import com.pedrotlf.barcalc.ui.components.PrimaryIconButton
import com.pedrotlf.barcalc.ui.components.QtyStepper
import com.pedrotlf.barcalc.ui.components.StepperSize
import com.pedrotlf.barcalc.ui.components.accentCheckboxColors
import com.pedrotlf.barcalc.ui.components.dashedBorder
import com.pedrotlf.barcalc.ui.components.roundedBorder
import com.pedrotlf.barcalc.ui.previewTabState
import com.pedrotlf.barcalc.ui.theme.BarCalcTheme
import com.pedrotlf.barcalc.ui.theme.BarTabColors
import com.pedrotlf.barcalc.ui.theme.BarTabDimens
import com.pedrotlf.barcalc.ui.theme.BarTabType

/** Screen 2 — who's splitting, claims, and the tip. */
@Composable
fun PeopleScreen(state: TabUiState, onAction: (TabAction) -> Unit) {
    val unclaimed = SplitCalculator.unclaimedInfo(state.items)
    val canCalculate = state.people.isNotEmpty() && unclaimed.isEmpty()

    ScreenScaffold(
        footer = {
            TipRow(state, onAction)
            if (unclaimed.isNotEmpty()) {
                // Resolve the template in composable scope, then fill it per item
                // (stringResource can't be called inside the joinToString lambda).
                val partialTemplate = stringResource(R.string.unclaimed_item_partial)
                val itemsLabel = unclaimed.joinToString(", ") { info ->
                    if (info.left == info.qty) info.name
                    else partialTemplate.format(info.name, info.left)
                }
                UnclaimedBanner(stringResource(R.string.unclaimed_banner, itemsLabel))
            }
            PrimaryButton(
                text = stringResource(R.string.calculate),
                onClick = { onAction(TabAction.GoToResults) },
                enabled = canCalculate,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        ScreenHeader(
            stringResource(R.string.people_title),
            stringResource(R.string.people_subtitle),
        )
        AddPersonCard(state, onAction)
        if (state.people.isNotEmpty()) {
            Column(
                Modifier.padding(
                    start = BarTabDimens.ScreenHPadding,
                    top = 4.dp,
                    end = BarTabDimens.ScreenHPadding,
                    bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(BarTabDimens.ListGap),
            ) {
                state.people.forEachIndexed { index, person ->
                    PersonRow(person, index, state, onAction)
                }
            }
        } else {
            EmptyListHint(stringResource(R.string.people_empty))
        }
    }
}

@Composable
private fun AddPersonCard(state: TabUiState, onAction: (TabAction) -> Unit) {
    Row(
        Modifier
            .padding(
                start = BarTabDimens.ScreenHPadding,
                top = 16.dp,
                end = BarTabDimens.ScreenHPadding,
                bottom = 8.dp,
            )
            .fillMaxWidth()
            .clip(RoundedCornerShape(BarTabDimens.RadiusLg))
            .background(BarTabColors.Accent100)
            .dashedBorder(BarTabColors.Accent300, cornerRadius = BarTabDimens.RadiusLg)
            .padding(BarTabDimens.CardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BarTabDimens.ListGap),
    ) {
        PillTextField(
            value = state.newPersonName,
            onValueChange = { onAction(TabAction.NewPersonNameChanged(it)) },
            placeholder = stringResource(R.string.person_name_hint),
            modifier = Modifier.weight(1f),
        )
        PrimaryIconButton(
            icon = AppIcons.Plus,
            contentDescription = stringResource(R.string.cd_add_person),
            onClick = { onAction(TabAction.AddPerson) },
            enabled = state.addPersonEnabled,
        )
    }
}

@Composable
private fun PersonRow(
    person: Person,
    index: Int,
    state: TabUiState,
    onAction: (TabAction) -> Unit,
) {
    val currency = LocalCurrencySymbol.current
    val unitCount = state.items.sumOf { SplitCalculator.personUnitCount(it, person.id) }
    val spendCents = SplitCalculator.personItemsTotal(state.items, person.id)
    val claimSummary = if (unitCount == 0) {
        stringResource(R.string.no_items_assigned)
    } else {
        pluralStringResource(
            R.plurals.claim_summary,
            unitCount,
            unitCount,
            SplitCalculator.formatMoney(spendCents, currency),
        )
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BarTabDimens.RadiusXl))
            .background(BarTabColors.Surface)
            .clickable { onAction(TabAction.OpenPerson(person.id)) }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BarTabDimens.ListGap),
    ) {
        Avatar(SplitCalculator.initialsFor(person.name), BarTabColors.avatarColor(index))
        Row(
            Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                person.name,
                style = BarTabType.RowTitle.copy(fontSize = 14.5.sp),
            )
            Text(
                claimSummary,
                style = BarTabType.Caption,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            AppIcons.ChevronRight,
            contentDescription = null,
            tint = BarTabColors.Neutral500,
            modifier = Modifier.size(16.dp),
        )
        GhostIconButton(
            icon = AppIcons.Trash,
            contentDescription = stringResource(R.string.cd_remove_person),
            onClick = { onAction(TabAction.RemovePerson(person.id)) },
            size = 28.dp,
            iconSize = 14.dp,
        )
    }
}

@Composable
private fun TipRow(state: TabUiState, onAction: (TabAction) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable { onAction(TabAction.ToggleTip) },
        ) {
            Checkbox(
                checked = state.tipEnabled,
                onCheckedChange = { onAction(TabAction.ToggleTip) },
                colors = accentCheckboxColors(),
            )
            Text(stringResource(R.string.add_tip), style = BarTabType.Label)
        }
        if (state.tipEnabled) {
            QtyStepper(
                label = stringResource(R.string.tip_percent, state.tipPercent),
                onDec = { onAction(TabAction.DecTip) },
                onInc = { onAction(TabAction.IncTip) },
                size = StepperSize.Tip,
            )
        }
    }
}

@Composable
private fun UnclaimedBanner(text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BarTabDimens.RadiusMd))
            .background(BarTabColors.Accent100)
            .roundedBorder(BarTabColors.Accent300, cornerRadius = BarTabDimens.RadiusMd)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            AppIcons.Warning,
            contentDescription = null,
            tint = BarTabColors.Accent700,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(15.dp),
        )
        Text(
            text,
            style = BarTabType.Body.copy(
                fontSize = 12.5.sp,
                color = BarTabColors.Accent700,
                lineHeight = 17.5.sp,
            ),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5EAD8, heightDp = 700)
@Composable
private fun PeopleScreenPreview() {
    BarCalcTheme {
        PeopleScreen(state = previewTabState(Screen.PEOPLE), onAction = {})
    }
}
