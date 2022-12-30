package br.com.pedrotlf.barcalc.domain.model

/**
 * [price] is in cents to help with calculations and mask functions. You should transform price when
 * using it. A simple "price.div(100)" should be enough.
 *
 * Ex.: if price = 100 it means 100 cents or 1.00.
 *
 * [totalPrice] calculation is already using the tranformed [price].
 * If [amount] is null, it uses 1 instead. It returns a simple [price] * [amount] after the
 * mentioned transformations.
 */
data class Order(
    val name: String? = null,
    val price: Int? = null,
    val amount: Int? = null
) {
    val totalPrice: Double
        get() {
            return if(price != null)
                price.div(100.0) * (amount ?: 1)
            else 0.0
        }
}