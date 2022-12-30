package br.com.pedrotlf.barcalc

object Utils {

    /**
     * Turns a double into a currency-like string. Ex.: 38.9512 turns into "38.95"
     */
    fun Double.toCurrencyString(): String = String.format("%.2f", this).replace(".", ",")

    fun Int.toAmountString(): String = "${this}x"
}