package com.pedrotlf.barcalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
        // Root screen: the nav slot holds the drawer toggle rather than a back
        // arrow, plus a shortcut to history — the most likely reason to open
        // the menu from here is to start a new tab from an old one.
        ScreenHeader(
            title = stringResource(R.string.items_title),
            subtitle = stringResource(R.string.items_subtitle),
            leading = { HeaderMenuButton { onAction(TabAction.OpenDrawer) } },
            action = { HeaderHistoryButton { onAction(TabAction.ShowHistory) } },
        )
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
        // The price field, quantity stepper and add button only share a row
        // when there's room. Below the threshold the stepper and button drop to
        // their own row, because otherwise the price — the only child with a
        // weight — is what gets squeezed, down to nothing on small screens.
        BoxWithConstraints {
            if (maxWidth < AddCardSingleRowMinWidth) {
                Column(verticalArrangement = Arrangement.spacedBy(BarTabDimens.ListGap)) {
                    PriceField(state, onAction, Modifier.fillMaxWidth())
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
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
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(BarTabDimens.ListGap),
                ) {
                    PriceField(state, onAction, Modifier.weight(1f))
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
    }
}

/**
 * Width the add card needs before the price, stepper and add button fit on one
 * row: the stepper and button take ~150dp of fixed width between them, leaving
 * the rest for a price box that still has to hold the currency symbol and the
 * "each" suffix around its input.
 */
private val AddCardSingleRowMinWidth = 320.dp

/** Price input on the app background: "<symbol> <amount> each". */
@Composable
private fun PriceField(
    state: TabUiState,
    onAction: (TabAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
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
                // Floor, so a long amount or a wide locale can never collapse
                // the input to a sliver again.
                .widthIn(min = 56.dp)
                .padding(horizontal = 4.dp, vertical = 10.dp),
        )
        Text(
            stringResource(R.string.price_each_suffix),
            style = BarTabType.Body.copy(fontSize = 13.sp, color = BarTabColors.Neutral600),
        )
    }
}

@Composable
private fun ItemRow(item: TabItem, onAction: (TabAction) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BarTabDimens.RadiusLg))
            .background(BarTabColors.Accent100)
            .roundedBorder(BarTabColors.Accent200, cornerRadius = BarTabDimens.RadiusLg)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        // Centred so the delete button sits mid-row whether the content takes
        // one line or wraps to two.
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Everything but the delete button, which stays pinned right. Below the
        // threshold the stepper and total drop to a second line, rather than
        // squeezing the name field down to a few characters.
        BoxWithConstraints(Modifier.weight(1f)) {
            if (maxWidth < ItemRowSingleRowMinWidth) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        ItemNameField(item, onAction, Modifier.weight(1f))
                        ItemPriceField(item, onAction)
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        ItemQtyStepper(item, onAction)
                        ItemTotal(item)
                    }
                }
            } else {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ItemNameField(item, onAction, Modifier.weight(1f))
                    ItemPriceField(item, onAction)
                    ItemQtyStepper(item, onAction)
                    ItemTotal(item)
                }
            }
        }
        GhostIconButton(
            icon = AppIcons.Trash,
            contentDescription = stringResource(R.string.cd_remove_item),
            onClick = { onAction(TabAction.RemoveItem(item.id)) },
            size = 28.dp,
            iconSize = 14.dp,
        )
    }
}

/**
 * Width an item row's content needs before the name, price, stepper and total
 * fit on one line. Under it the name field would be squeezed to a few
 * characters, since it is the only part that flexes.
 */
private val ItemRowSingleRowMinWidth = 280.dp

@Composable
private fun ItemNameField(
    item: TabItem,
    onAction: (TabAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    BareTextField(
        value = item.name,
        onValueChange = { onAction(TabAction.ItemNameChanged(item.id, it)) },
        textStyle = BarTabType.RowTitle,
        keyboardOptions = NameCapitalization,
        modifier = modifier,
    )
}

/** Editable unit price — the box grows with the amount so large values aren't hidden. */
@Composable
private fun ItemPriceField(item: TabItem, onAction: (TabAction) -> Unit) {
    val currency = LocalCurrencySymbol.current
    val priceStyle = BarTabType.Caption.copy(color = BarTabColors.Neutral700)
    Row(
        Modifier
            .clip(RoundedCornerShape(BarTabDimens.RadiusSm))
            .background(BarTabColors.Bg)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(currency, style = BarTabType.Caption)
        Box(contentAlignment = Alignment.CenterStart) {
            // Invisible sizer gives the field exactly the width of its text.
            Text(
                SplitCalculator.formatMoney(item.priceCents, symbol = ""),
                style = priceStyle,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .widthIn(min = 26.dp)
                    .padding(end = 2.dp) // caret room so the first digit never clips
                    .alpha(0f),
            )
            MoneyField(
                cents = item.priceCents,
                onCentsChange = { onAction(TabAction.ItemPriceChanged(item.id, it)) },
                textStyle = priceStyle,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@Composable
private fun ItemQtyStepper(item: TabItem, onAction: (TabAction) -> Unit) {
    QtyStepper(
        label = "${item.qty}",
        onDec = { onAction(TabAction.DecItemQty(item.id)) },
        onInc = { onAction(TabAction.IncItemQty(item.id)) },
        size = StepperSize.Compact,
    )
}

@Composable
private fun ItemTotal(item: TabItem) {
    Text(
        SplitCalculator.formatMoney(item.priceCents * item.qty, LocalCurrencySymbol.current),
        style = BarTabType.Money,
        textAlign = TextAlign.End,
        modifier = Modifier.widthIn(min = 44.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF5EAD8, heightDp = 700)
@Composable
private fun ItemsScreenPreview() {
    BarCalcTheme {
        ItemsScreen(state = previewTabState(), onAction = {})
    }
}

/** Narrow enough that the add card drops its stepper and button to a second row. */
@Preview(
    name = "Narrow — stacked add card",
    showBackground = true,
    backgroundColor = 0xFFF5EAD8,
    heightDp = 700,
    widthDp = 320,
)
@Composable
private fun ItemsScreenNarrowPreview() {
    BarCalcTheme {
        ItemsScreen(state = previewTabState(), onAction = {})
    }
}
