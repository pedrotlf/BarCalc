package br.com.pedrotlf.barcalc.domain.model

data class Order(
    val name: String? = null,
    val price: Double? = null,
    val amount: Int = 1
) {
    val totalPrice: Double
        get() = if(price != null) price * amount else 0.0
}