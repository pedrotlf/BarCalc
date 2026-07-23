package com.pedrotlf.barcalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrotlf.barcalc.R
import com.pedrotlf.barcalc.domain.Person
import com.pedrotlf.barcalc.domain.SplitCalculator
import com.pedrotlf.barcalc.domain.TabItem
import com.pedrotlf.barcalc.ui.Screen
import com.pedrotlf.barcalc.ui.TabAction
import com.pedrotlf.barcalc.ui.TabUiState
import com.pedrotlf.barcalc.ui.components.AppIcons
import com.pedrotlf.barcalc.ui.components.Avatar
import com.pedrotlf.barcalc.ui.components.GhostIconButton
import com.pedrotlf.barcalc.ui.components.LocalCurrencySymbol
import com.pedrotlf.barcalc.ui.components.PrimaryButton
import com.pedrotlf.barcalc.ui.components.accentCheckboxColors
import com.pedrotlf.barcalc.ui.components.bottomBorder
import com.pedrotlf.barcalc.ui.components.roundedBorder
import com.pedrotlf.barcalc.ui.previewTabState
import com.pedrotlf.barcalc.ui.theme.BarCalcTheme
import com.pedrotlf.barcalc.ui.theme.BarTabColors
import com.pedrotlf.barcalc.ui.theme.BarTabDimens
import com.pedrotlf.barcalc.ui.theme.BarTabType

/**
 * Centered modal where [person] claims units of each item. Scrim tap or
 * Done closes it.
 */
@Composable
fun ClaimSheet(
    person: Person,
    personIndex: Int,
    state: TabUiState,
    onAction: (TabAction) -> Unit,
) {
    val scrimInteraction = remember { MutableInteractionSource() }
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(BarTabColors.Scrim)
            .clickable(
                interactionSource = scrimInteraction,
                indication = null,
            ) { onAction(TabAction.CloseSheet) }
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        val sheetMaxHeight = maxHeight * 0.76f
        val sheetWidth = (maxWidth * BarTabDimens.SheetWidthFraction)
            .coerceAtMost(BarTabDimens.SheetMaxWidth)
        Column(
            Modifier
                .width(sheetWidth)
                .heightIn(max = sheetMaxHeight)
                .shadow(24.dp, RoundedCornerShape(BarTabDimens.RadiusLg))
                .clip(RoundedCornerShape(BarTabDimens.RadiusLg))
                .background(BarTabColors.Bg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { /* swallow clicks so the scrim doesn't close */ },
        ) {
            SheetHeader(person, personIndex, onAction)
            Text(
                stringResource(R.string.claim_instructions),
                style = BarTabType.Body.copy(fontSize = 11.5.sp, color = BarTabColors.Neutral600),
                modifier = Modifier.padding(start = 16.dp, top = 10.dp, end = 16.dp),
            )
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                state.items.forEach { item ->
                    ClaimRow(item, person, onAction)
                }
            }
            Box(Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp)) {
                PrimaryButton(
                    text = stringResource(R.string.done),
                    onClick = { onAction(TabAction.CloseSheet) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SheetHeader(person: Person, personIndex: Int, onAction: (TabAction) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .bottomBorder(BarTabColors.Accent200)
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Avatar(
            SplitCalculator.initialsFor(person.name),
            BarTabColors.avatarColor(personIndex),
            size = 36.dp,
            fontSize = 14.sp,
        )
        Text(
            person.name,
            style = BarTabType.Body.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f),
        )
        GhostIconButton(
            icon = AppIcons.Close,
            contentDescription = stringResource(R.string.cd_close),
            onClick = { onAction(TabAction.CloseSheet) },
            size = 36.dp,
            iconSize = 16.dp,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClaimRow(item: TabItem, person: Person, onAction: (TabAction) -> Unit) {
    val currency = LocalCurrencySymbol.current
    Row(
        Modifier
            .fillMaxWidth()
            .bottomBorder(BarTabColors.Accent100)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BarTabDimens.ListGap),
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.name, style = BarTabType.RowTitle)
            Text(
                stringResource(
                    R.string.unit_price_each,
                    SplitCalculator.formatMoney(item.priceCents, currency),
                ),
                style = BarTabType.Caption,
            )
        }
        if (item.qty == 1) {
            Checkbox(
                checked = person.id in item.units[0],
                onCheckedChange = {
                    onAction(TabAction.ToggleUnitClaim(item.id, 0, person.id))
                },
                colors = accentCheckboxColors(),
            )
        } else {
            val allClaimed = item.allUnitsClaimedBy(person.id)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.widthIn(max = 160.dp),
            ) {
                // One tap to claim (or release) every unit at once.
                AllChip(
                    active = allClaimed,
                    onClick = {
                        onAction(TabAction.SetAllUnitsClaim(item.id, person.id, !allClaimed))
                    },
                )
                item.units.forEachIndexed { unitIndex, unit ->
                    UnitChip(
                        index = unitIndex + 1,
                        active = person.id in unit,
                        totalCount = unit.size,
                        onClick = {
                            onAction(TabAction.ToggleUnitClaim(item.id, unitIndex, person.id))
                        },
                    )
                }
            }
        }
    }
}

/** Chip that claims or releases every unit of an item at once. */
@Composable
private fun AllChip(active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .defaultMinSize(minWidth = 30.dp, minHeight = 30.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (active) BarTabColors.Accent500 else BarTabColors.Bg)
            .roundedBorder(
                color = if (active) BarTabColors.Accent500 else BarTabColors.Accent300,
                cornerRadius = 9.dp,
                strokeWidth = 1.5.dp,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(R.string.claim_all),
            style = BarTabType.Body.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (active) Color.White else BarTabColors.Neutral600,
            ),
        )
    }
}

@Composable
private fun UnitChip(index: Int, active: Boolean, totalCount: Int, onClick: () -> Unit) {
    Box {
        Box(
            Modifier
                .defaultMinSize(minWidth = 30.dp, minHeight = 30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (active) BarTabColors.Accent500 else BarTabColors.Bg)
                .roundedBorder(
                    color = if (active) BarTabColors.Accent500 else BarTabColors.Accent300,
                    cornerRadius = 9.dp,
                    strokeWidth = 1.5.dp,
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$index",
                style = BarTabType.Body.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (active) Color.White else BarTabColors.Neutral600,
                ),
            )
        }
        if (totalCount > 1) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 5.dp, y = (-5).dp)
                    .defaultMinSize(minWidth = 14.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(BarTabColors.Accent700)
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "$totalCount",
                    style = BarTabType.Body.copy(fontSize = 9.sp, color = Color.White),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5EAD8, heightDp = 700)
@Composable
private fun ClaimSheetPreview() {
    val state = previewTabState(Screen.PEOPLE)
    BarCalcTheme {
        ClaimSheet(
            person = state.people.first(),
            personIndex = 0,
            state = state,
            onAction = {},
        )
    }
}
