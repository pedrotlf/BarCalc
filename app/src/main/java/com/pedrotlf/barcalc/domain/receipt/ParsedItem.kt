package com.pedrotlf.barcalc.domain.receipt

/**
 * One line the parser believes is an item on the tab.
 *
 * [priceCents] is the price of a *single* unit, matching [com.pedrotlf.barcalc
 * .domain.TabItem], so a parsed item can become a tab item without further
 * arithmetic.
 */
data class ParsedItem(
    val name: String,
    val priceCents: Long,
    val qty: Int,
)
