package br.com.pedrotlf.barcalc.presentation.orders

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.pedrotlf.barcalc.domain.model.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(): ViewModel() {

    var orderList = mutableStateOf<List<Order>>(listOf())

    fun addOrder() = viewModelScope.launch {
        orderList.value += Order()
    }

    fun editOrder(index: Int, order: Order) {
        editOrder(index, order.name, order.price, order.amount)
    }

    fun editOrder(index: Int, name: String?, price: Int?, amount: Int?) = viewModelScope.launch {
        val list = orderList.value
        orderList.value = list.toMutableList().also {
            it[index] = Order(name, price, amount)
        }
    }
}