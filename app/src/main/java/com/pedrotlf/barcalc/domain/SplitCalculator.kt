package com.pedrotlf.barcalc.domain

enum class TipSplitMode { EVEN, PROPORTIONAL }

/**
 * Pure split math over integer cents, ported from the design's DCLogic
 * reference implementation.
 *
 * Rounding policy: whenever an amount doesn't divide evenly, leftover cents are
 * handed out one at a time by largest-remainder allocation, so every unit's
 * shares sum exactly to its price and the tip shares sum exactly to the tip.
 * (Within a single unit the sharers are equal, so the earliest simply win the
 * ties.)
 */
object SplitCalculator {

    /** Tip is shared in proportion to each person's own share of the tab. */
    val TIP_SPLIT_MODE: TipSplitMode = TipSplitMode.PROPORTIONAL

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
     * Split [tipCents] across participants whose item subtotals are [itemTotals]
     * (in the same order), returning each person's tip in cents.
     *
     * - [TipSplitMode.PROPORTIONAL] weights each share by that person's own item
     *   total, so someone who only had a $10 item pays tip on $10, not on the
     *   whole tab. A participant with no items pays no tip.
     * - [TipSplitMode.EVEN] weights everyone equally.
     *
     * Either way the shares sum *exactly* to [tipCents]: leftover cents from
     * rounding go to the largest remainders, ties broken by position. If nobody
     * has any items (so proportional weights are all zero) it falls back to an
     * even split.
     */
    fun allocateTip(
        itemTotals: List<Long>,
        tipCents: Long,
        mode: TipSplitMode = TIP_SPLIT_MODE,
    ): List<Long> {
        val n = itemTotals.size
        if (n == 0 || tipCents == 0L) return List(n) { 0L }

        val proportional = mode == TipSplitMode.PROPORTIONAL && itemTotals.sum() > 0L
        val weights = if (proportional) itemTotals else List(n) { 1L }
        val totalWeight = weights.sum()

        val base = LongArray(n)
        val remainders = LongArray(n)
        var allocated = 0L
        for (i in 0 until n) {
            val numerator = tipCents * weights[i]
            base[i] = numerator / totalWeight
            remainders[i] = numerator % totalWeight
            allocated += base[i]
        }

        // Hand out the leftover cents to the largest remainders (earliest on ties).
        val byRemainder = (0 until n).sortedWith(
            compareByDescending<Int> { remainders[it] }.thenBy { it }
        )
        var leftover = tipCents - allocated
        var i = 0
        while (leftover > 0 && i < byRemainder.size) {
            base[byRemainder[i]] += 1L
            leftover--
            i++
        }
        return base.toList()
    }

    /** Everything the person owes for their items (before tip), in cents. */
    fun personItemsTotal(items: List<TabItem>, personId: Int): Long =
        items.sumOf { personItemCost(it, personId) }

    /** 1234 cents -> "$12.34" (pass an empty symbol for a plain "12.34"). */
    fun formatMoney(cents: Long, symbol: String): String =
        "$symbol${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"

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
