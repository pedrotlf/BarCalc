package com.pedrotlf.barcalc.domain

import kotlinx.serialization.Serializable

/**
 * One line of the bar tab. Prices are integer cents so amounts always add up
 * exactly — no floating-point drift.
 *
 * [units] always has [qty] entries; each entry is the list of person ids
 * claiming that physical unit, in claim order. A unit shared by N people
 * splits its price N ways (see [SplitCalculator]). An empty entry means that
 * unit is still unclaimed.
 */
@Serializable
data class TabItem(
    val id: Int,
    val name: String,
    val priceCents: Long,
    val qty: Int,
    val units: List<List<Int>>,
) {
    companion object {
        fun new(id: Int, name: String, priceCents: Long, qty: Int): TabItem =
            TabItem(id, name, priceCents, qty, List(qty) { emptyList() })
    }

    /** qty + 1, appending a fresh unclaimed unit (mirrors the design's incItemQty). */
    fun withQtyIncremented(): TabItem =
        copy(qty = qty + 1, units = units + listOf(emptyList()))

    /** qty - 1 (min 1), dropping the last unit and its claims (mirrors decItemQty). */
    fun withQtyDecremented(): TabItem =
        if (qty <= 1) this else copy(qty = qty - 1, units = units.take(qty - 1))

    /** Toggle [personId]'s claim on unit [unitIndex]. */
    fun withClaimToggled(unitIndex: Int, personId: Int): TabItem {
        val newUnits = units.mapIndexed { idx, unit ->
            when {
                idx != unitIndex -> unit
                personId in unit -> unit - personId
                else -> unit + personId
            }
        }
        return copy(units = newUnits)
    }

    /** Remove every claim by [personId] (used when a person is deleted). */
    fun withPersonRemoved(personId: Int): TabItem =
        copy(units = units.map { unit -> unit.filter { it != personId } })
}
