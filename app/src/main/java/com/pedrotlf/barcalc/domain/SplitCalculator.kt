package com.pedrotlf.barcalc.domain

import java.math.BigDecimal
import java.math.RoundingMode

enum class TipSplitMode { EVEN, PROPORTIONAL }

/**
 * Pure split math over integer cents, ported from the design's DCLogic
 * reference implementation.
 *
 * Rounding policy: whenever an amount doesn't divide evenly, the leftover
 * cents go to the earliest participants (largest-remainder allocation), so
 * every unit's shares sum exactly to its price and the tip shares sum exactly
 * to the tip.
 */
object SplitCalculator {

    val TIP_SPLIT_MODE: TipSplitMode = TipSplitMode.EVEN

    /** [personId]'s share of one unit: price / sharers; earliest claimers absorb leftover cents. */
    private fun unitShareCents(priceCents: Long, unit: List<Int>, personId: Int): Long {
        val index = unit.indexOf(personId)
        if (index < 0) return 0L
        val base = priceCents / unit.size
        val remainder = priceCents % unit.size
        return base + if (index < remainder) 1L else 0L
    }

    /** What [personId] owes for [item], in cents. */
    fun personItemCost(item: TabItem, personId: Int): Long =
        item.units.sumOf { unit -> unitShareCents(item.priceCents, unit, personId) }

    /** How many units of [item] the person is part of. */
    fun personUnitCount(item: TabItem, personId: Int): Int =
        item.units.count { personId in it }

    fun subtotal(items: List<TabItem>): Long =
        items.sumOf { it.priceCents * it.qty }

    /** Items that still have at least one unclaimed unit. */
    fun unclaimedItems(items: List<TabItem>): List<TabItem> =
        items.filter { item -> item.units.any { it.isEmpty() } }

    /** Name + how many of its units are still unclaimed. [left] == [qty] means none claimed. */
    data class Unclaimed(val name: String, val left: Int, val qty: Int)

    /** Structured "what still needs claiming"; the UI formats the label so it can localize. */
    fun unclaimedInfo(items: List<TabItem>): List<Unclaimed> =
        unclaimedItems(items).map { item ->
            Unclaimed(item.name, item.units.count { it.isEmpty() }, item.qty)
        }

    /** Tip in cents, rounded half-up. */
    fun tipAmount(subtotalCents: Long, tipEnabled: Boolean, tipPercent: Int): Long =
        if (tipEnabled) (subtotalCents * tipPercent + 50) / 100 else 0L

    /**
     * This person's share of the tip, in cents. Even mode allocates leftover
     * cents to the first people on the list.
     */
    fun personTipShare(
        personIndex: Int,
        peopleCount: Int,
        tipCents: Long,
        personItemsCents: Long = 0L,
        subtotalCents: Long = 0L,
        mode: TipSplitMode = TIP_SPLIT_MODE,
    ): Long = when {
        tipCents == 0L || peopleCount == 0 -> 0L
        mode == TipSplitMode.PROPORTIONAL && subtotalCents > 0 ->
            // round(personItems * tip / subtotal), half-up in integer math
            (2 * personItemsCents * tipCents + subtotalCents) / (2 * subtotalCents)
        else -> tipCents / peopleCount + if (personIndex < tipCents % peopleCount) 1L else 0L
    }

    /** Everything the person owes for their items (before tip), in cents. */
    fun personItemsTotal(items: List<TabItem>, personId: Int): Long =
        items.sumOf { personItemCost(it, personId) }

    /** 1234 cents -> "$12.34". */
    fun formatMoney(cents: Long, symbol: String): String =
        "$symbol${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"

    /** "12.50" or "12,50" -> 1250 cents; blank/garbage -> null. Sub-cent digits round half-up. */
    fun parsePriceCents(raw: String): Long? =
        raw.trim()
            .replace(',', '.')
            .toBigDecimalOrNull()
            ?.movePointRight(2)
            ?.setScale(0, RoundingMode.HALF_UP)
            ?.toLong()

    /** 1250 cents -> "12.5", 1000 -> "10", 0 -> "" — a friendly editable representation. */
    fun formatPriceForEdit(cents: Long): String =
        if (cents == 0L) ""
        else BigDecimal(cents).movePointLeft(2).stripTrailingZeros().toPlainString()

    /** "Ana Silva" -> "AS"; single names use the first letter; blank -> "?". */
    fun initialsFor(name: String): String {
        val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val initials = buildString {
            parts.getOrNull(0)?.firstOrNull()?.let { append(it) }
            parts.getOrNull(1)?.firstOrNull()?.let { append(it) }
        }.uppercase()
        return initials.ifEmpty { "?" }
    }
}
