package br.com.pedrotlf.barcalc.presentation.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.sharp.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import br.com.pedrotlf.barcalc.PriceMaskTranformation
import br.com.pedrotlf.barcalc.Utils.toCurrencyString
import br.com.pedrotlf.barcalc.domain.model.Order

@Composable
fun OrderListScreen(
    navController: NavController,
    viewModel: OrderViewModel = hiltViewModel()
) {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            OrderList(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                viewModel = viewModel
            )
            ExtendedFloatingActionButton(
                onClick = {
                    //TODO
                },
                icon = {
                    Icon(
                        imageVector = Icons.Sharp.ArrowForward,
                        contentDescription = "Next"
                    )
                },
                text = {
                    Text(text = "Next")
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun OrderList(
    modifier: Modifier,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val orderList by remember { viewModel.orderList }
    val listState = rememberLazyListState()

    LaunchedEffect(orderList.size) {
        listState.animateScrollToItem(orderList.size + 1)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(orderList) { index, order ->
            OrderCard(
                modifier = Modifier,
                order = order,
                onValueChangedCallback = { newOrder ->
                    viewModel.editOrder(index, newOrder)
                }
            )
        }
        item {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.addOrder()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add an order"
                    )
                },
                text = {
                    Text(text = "Add an order")
                }
            )
        }
    }
}

@Composable
fun OrderCard(
    modifier: Modifier,
    order: Order,
    onValueChangedCallback: (newOrder: Order) -> Unit
) {
    var amount by remember { mutableStateOf(order.amount.toString()) }
    var name by remember { mutableStateOf(order.name) }
    var price by remember { mutableStateOf(order.price?.toString()?.filter { it.isDigit() }) }
    val maxAmountLenght = 3
    val maxNameLenght = 32

    Box(
        modifier = modifier
            .shadow(5.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Cyan)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(0.2f),
                    value = amount,
                    onValueChange = {
                        if(it.length <= maxAmountLenght)
                            amount = it
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    label = { Text(text = "Nº") },
                    placeholder = { Text(text = "Nº") }
                )
                OutlinedTextField(
                    modifier = Modifier.weight(0.8f),
                    singleLine = true,
                    value = name.orEmpty(),
                    onValueChange = {
                        if(it.length <= maxNameLenght)
                            name = it
                    },
                    label = { Text(text = "Name") },
                    placeholder = { Text(text = "Item's name") }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(intrinsicSize = IntrinsicSize.Max),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(0.4f, false),
                    singleLine = true,
                    value = price.orEmpty(),
                    onValueChange = {
                        price = if (it.startsWith("0")) {
                            ""
                        } else {
                            it
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    label = { Text(text = "Price") },
                    placeholder = { Text(text = "Item's price") },
                    visualTransformation = PriceMaskTranformation()
                )
                Text(
                    modifier = Modifier.weight(0.4f, false),
                    text = "Total: ${order.totalPrice.toCurrencyString()}"
                )
            }
        }
    }
}