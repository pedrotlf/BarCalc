package com.pedrotlf.barcalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrotlf.barcalc.R
import com.pedrotlf.barcalc.domain.SplitCalculator
import com.pedrotlf.barcalc.domain.TabItem
import com.pedrotlf.barcalc.ui.TabAction
import com.pedrotlf.barcalc.ui.TabUiState
import com.pedrotlf.barcalc.ui.components.AppIcons
import com.pedrotlf.barcalc.ui.components.BareTextField
import com.pedrotlf.barcalc.ui.components.GhostIconButton
import com.pedrotlf.barcalc.ui.components.LocalCurrencySymbol
import com.pedrotlf.barcalc.ui.components.MoneyField
import com.pedrotlf.barcalc.ui.components.NameCapitalization
import com.pedrotlf.barcalc.ui.components.PillTextField
import com.pedrotlf.barcalc.ui.components.PrimaryButton
import com.pedrotlf.barcalc.ui.components.PrimaryIconButton
import com.pedrotlf.barcalc.ui.components.QtyStepper
import com.pedrotlf.barcalc.ui.components.StepperSize
import com.pedrotlf.barcalc.ui.components.dashedBorder
import com.pedrotlf.barcalc.ui.components.roundedBorder
import com.pedrotlf.barcalc.ui.previewTabState
import com.pedrotlf.barcalc.ui.theme.BarCalcTheme
import com.pedrotlf.barcalc.ui.theme.BarTabColors
import com.pedrotlf.barcalc.ui.theme.BarTabDimens
import com.pedrotlf.barcalc.ui.theme.BarTabType

/** Screen 1 — build the tab item by item. */
@Composable
fun ItemsScreen(state: TabUiState, onAction: (TabAction) -> Unit) {
    val currency = LocalCurrencySymbol.current
    ScreenScaffold(
        footer = {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.subtotal), style = BarTabType.LabelMuted)
                Text(
                    SplitCalculator.formatMoney(SplitCalculator.subtotal(state.items), currency),
                    style = BarTabType.Label.copy(fontWeight = FontWeight.Bold),
                )
            }
            PrimaryButton(
                text = stringResource(R.string.next),
                onClick = { onAction(TabAction.GoToPeople) },
                enabled = state.items.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.items.isEmpty()) {
                Text(
                    stringResource(R.string.add_item_disabled_hint),
                    style = BarTabType.Hint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) {
        ScreenHeader(stringResource(R.string.items_title), stringResource(R.string.items_subtitle))
        AddItemCard(state, onAction)
        if (state.items.isNotEmpty()) {
            Column(
                Modifier.padding(
                    start = BarTabDimens.ScreenHPadding,
                    top = 4.dp,
                    end = BarTabDimens.ScreenHPadding,
                    bottom = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(BarTabDimens.ListGap),
            ) {
                state.items.forEach { item ->
                    key(item.id) {
                        ItemRow(item, onAction)
                    }
                }
            }
        } else {
            EmptyListHint(stringResource(R.string.items_empty))
        }
    }
}

@Composable
private fun AddItemCard(state: TabUiState, onAction: (TabAction) -> Unit) {
    Column(
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
        verticalArrangement = Arrangement.spacedBy(BarTabDimens.ListGap),
    ) {
        PillTextField(
            value = state.newItemName,
            onValueChange = { onAction(TabAction.NewItemNameChanged(it)) },
            placeholder = stringResource(R.string.item_name_hint),
            keyboardOptions = NameCapitalization,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BarTabDimens.ListGap),
        ) {
            // $ price field on the app background
            Row(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(BarTabDimens.RadiusMd))
                    .background(BarTabColors.Bg)
                    .padding(horizontal = 10.dp)
                    .defaultMinSize(minHeight = 44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    LocalCurrencySymbol.current,
                    style = BarTabType.Body.copy(fontSize = 15.sp, color = BarTabColors.Neutral700),
                )
                MoneyField(
                    cents = state.newItemPriceCents,
                    onCentsChange = { onAction(TabAction.NewItemPriceChanged(it)) },
                    textStyle = BarTabType.Body.copy(fontSize = 15.sp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp, vertical = 10.dp),
                )
                Text(
                    stringResource(R.string.price_each_suffix),
                    style = BarTabType.Body.copy(fontSize = 13.sp, color = BarTabColors.Neutral600),
                )
            }
            QtyStepper(
                label = "${state.newItemQty}",
                onDec = { onAction(TabAction.DecNewQty) },
                onInc = { onAction(TabAction.IncNewQty) },
            )
            PrimaryIconButton(
                icon = AppIcons.Plus,
                contentDescription = stringResource(R.string.cd_add_item),
                onClick = { onAction(TabAction.AddItem) },
                enabled = state.addItemEnabled,
            )
        }
    }
}

@Composable
private fun ItemRow(item: TabItem, onAction: (TabAction) -> Unit) {
    val currency = LocalCurrencySymbol.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BarTabDimens.RadiusLg))
            .background(BarTabColors.Accent100)
            .roundedBorder(BarTabColors.Accent200, cornerRadius = BarTabDimens.RadiusLg)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BareTextField(
            value = item.name,
            onValueChange = { onAction(TabAction.ItemNameChanged(item.id, it)) },
            textStyle = BarTabType.RowTitle,
            keyboardOptions = NameCapitalization,
            modifier = Modifier.weight(1f),
        )
        // Small editable price
        Row(
            Modifier
                .width(64.dp)
                .clip(RoundedCornerShape(BarTabDimens.RadiusSm))
                .background(BarTabColors.Bg)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(currency, style = BarTabType.Caption)
            MoneyField(
                cents = item.priceCents,
                onCentsChange = { onAction(TabAction.ItemPriceChanged(item.id, it)) },
                textStyle = BarTabType.Caption.copy(color = BarTabColors.Neutral700),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp),
            )
        }
        QtyStepper(
            label = "${item.qty}",
            onDec = { onAction(TabAction.DecItemQty(item.id)) },
            onInc = { onAction(TabAction.IncItemQty(item.id)) },
            size = StepperSize.Compact,
        )
        Text(
            SplitCalculator.formatMoney(item.priceCents * item.qty, currency),
            style = BarTabType.Money,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(min = 44.dp),
        )
        GhostIconButton(
            icon = AppIcons.Trash,
            contentDescription = stringResource(R.string.cd_remove_item),
            onClick = { onAction(TabAction.RemoveItem(item.id)) },
            size = 28.dp,
            iconSize = 14.dp,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5EAD8, heightDp = 700)
@Composable
private fun ItemsScreenPreview() {
    BarCalcTheme {
        ItemsScreen(state = previewTabState(), onAction = {})
    }
}
