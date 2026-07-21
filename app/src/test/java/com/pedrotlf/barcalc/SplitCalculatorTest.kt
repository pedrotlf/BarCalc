package com.pedrotlf.barcalc

import com.pedrotlf.barcalc.domain.SplitCalculator
import com.pedrotlf.barcalc.domain.TabItem
import com.pedrotlf.barcalc.domain.TipSplitMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SplitCalculatorTest {

    private val alice = 1
    private val bob = 2
    private val carol = 3

    /** 3 beers at $10.00: Alice claims units 0+1, both share unit 2. */
    private fun sharedBeers() = TabItem(
        id = 1, name = "Beer", priceCents = 1000L, qty = 3,
        units = listOf(listOf(alice), listOf(alice), listOf(alice, bob)),
    )

    @Test
    fun `unshared units cost full price, shared units split evenly`() {
        val item = sharedBeers()
        // Alice: 1000 + 1000 + 500 = 2500¢; Bob: 500¢
        assertEquals(2500L, SplitCalculator.personItemCost(item, alice))
        assertEquals(500L, SplitCalculator.personItemCost(item, bob))
    }

    @Test
    fun `uneven unit split gives leftover cents to earliest claimers and sums exactly`() {
        // $10.00 shared by three -> 334 + 333 + 333 = 1000
        val item = TabItem(
            id = 1, name = "Beer", priceCents = 1000L, qty = 1,
            units = listOf(listOf(alice, bob, carol)),
        )
        val shares = listOf(alice, bob, carol).map { SplitCalculator.personItemCost(item, it) }
        assertEquals(listOf(334L, 333L, 333L), shares)
        assertEquals(item.priceCents, shares.sum())
    }

    @Test
    fun `person unit count counts every unit they touch`() {
        val item = sharedBeers()
        assertEquals(3, SplitCalculator.personUnitCount(item, alice))
        assertEquals(1, SplitCalculator.personUnitCount(item, bob))
    }

    @Test
    fun `subtotal is price times qty across items`() {
        val items = listOf(sharedBeers(), TabItem.new(2, "Nachos", 1200L, 1))
        assertEquals(4200L, SplitCalculator.subtotal(items))
    }

    @Test
    fun `new item starts fully unclaimed`() {
        val item = TabItem.new(2, "Nachos", 1200L, 2)
        assertEquals(2, item.units.size)
        assertTrue(item.units.all { it.isEmpty() })
        assertEquals(listOf(item), SplitCalculator.unclaimedItems(listOf(item)))
    }

    @Test
    fun `unclaimed info reports partial counts`() {
        val nachos = TabItem.new(2, "Nachos", 1200L, 1)
        val partialBeer = TabItem(
            id = 1, name = "Beer", priceCents = 1000L, qty = 3,
            units = listOf(listOf(alice), emptyList(), emptyList()),
        )
        assertEquals(
            listOf(
                SplitCalculator.Unclaimed("Beer", left = 2, qty = 3),
                SplitCalculator.Unclaimed("Nachos", left = 1, qty = 1),
            ),
            SplitCalculator.unclaimedInfo(listOf(partialBeer, nachos)),
        )
    }

    @Test
    fun `fully claimed tab has no unclaimed items`() {
        assertTrue(SplitCalculator.unclaimedItems(listOf(sharedBeers())).isEmpty())
        assertTrue(SplitCalculator.unclaimedInfo(listOf(sharedBeers())).isEmpty())
    }

    @Test
    fun `tip amount respects toggle and percent and rounds half-up`() {
        assertEquals(300L, SplitCalculator.tipAmount(3000L, true, 10))
        assertEquals(0L, SplitCalculator.tipAmount(3000L, false, 10))
        assertEquals(0L, SplitCalculator.tipAmount(3000L, true, 0))
        // 1005 * 10% = 100.5 -> 101
        assertEquals(101L, SplitCalculator.tipAmount(1005L, true, 10))
    }

    @Test
    fun `even tip splits equally and allocates leftover cents to first people`() {
        // 100¢ across 3 people -> 34 + 33 + 33 = 100
        val shares = (0 until 3).map { index ->
            SplitCalculator.personTipShare(
                personIndex = index, peopleCount = 3, tipCents = 100L,
                mode = TipSplitMode.EVEN,
            )
        }
        assertEquals(listOf(34L, 33L, 33L), shares)
        assertEquals(100L, shares.sum())
    }

    @Test
    fun `proportional tip follows consumption`() {
        val share = SplitCalculator.personTipShare(
            personIndex = 0, peopleCount = 2, tipCents = 300L,
            personItemsCents = 2500L, subtotalCents = 3000L,
            mode = TipSplitMode.PROPORTIONAL,
        )
        assertEquals(250L, share)
    }

    @Test
    fun `qty increment appends an unclaimed unit and keeps claims`() {
        val item = sharedBeers().withQtyIncremented()
        assertEquals(4, item.qty)
        assertEquals(4, item.units.size)
        assertEquals(listOf(alice, bob), item.units[2])
        assertTrue(item.units[3].isEmpty())
    }

    @Test
    fun `qty decrement drops the last unit but never goes below 1`() {
        val item = sharedBeers().withQtyDecremented()
        assertEquals(2, item.qty)
        assertEquals(listOf(listOf(alice), listOf(alice)), item.units)

        val single = TabItem.new(2, "Nachos", 1200L, 1)
        assertEquals(single, single.withQtyDecremented())
    }

    @Test
    fun `claim toggle adds then removes a person on one unit`() {
        val item = TabItem.new(1, "Beer", 1000L, 2)
        val claimed = item.withClaimToggled(1, bob)
        assertEquals(listOf(bob), claimed.units[1])
        assertTrue(claimed.units[0].isEmpty())
        val unclaimed = claimed.withClaimToggled(1, bob)
        assertTrue(unclaimed.units[1].isEmpty())
    }

    @Test
    fun `removing a person strips all their claims`() {
        val item = sharedBeers().withPersonRemoved(alice)
        assertEquals(listOf(emptyList<Int>(), emptyList(), listOf(bob)), item.units)
    }

    @Test
    fun `claim all units adds person to every unit and preserves others`() {
        // Bob already shares unit 2 in sharedBeers(); Alice claims all.
        val item = sharedBeers().withAllUnitsClaimed(bob, claimed = true)
        assertTrue(item.allUnitsClaimedBy(bob))
        // Alice's pre-existing claims on units 0 and 1 are untouched.
        assertTrue(item.units.all { alice in it })
    }

    @Test
    fun `release all units removes only that person`() {
        val item = sharedBeers().withAllUnitsClaimed(alice, claimed = false)
        assertTrue(item.units.none { alice in it })
        // Bob's share of unit 2 remains.
        assertEquals(listOf(bob), item.units[2])
        assertFalse(item.allUnitsClaimedBy(alice))
    }

    @Test
    fun `allUnitsClaimedBy is false when any unit is unclaimed by the person`() {
        val partial = TabItem.new(1, "Beer", 1000L, 3).withClaimToggled(0, alice)
        assertFalse(partial.allUnitsClaimedBy(alice))
    }

    @Test
    fun `plan happy path - shared beer plus even tip`() {
        // 3 beers $10 + nachos $12; Alice takes 2 beers + shares 1 with Bob,
        // Bob has nachos + the shared beer; 10% tip split evenly.
        val beers = sharedBeers()
        val nachos = TabItem(2, "Nachos", 1200L, 1, listOf(listOf(bob)))
        val items = listOf(beers, nachos)

        val subtotal = SplitCalculator.subtotal(items)
        assertEquals(4200L, subtotal)
        val tip = SplitCalculator.tipAmount(subtotal, true, 10)
        assertEquals(420L, tip)

        val aliceItems = SplitCalculator.personItemsTotal(items, alice)
        val bobItems = SplitCalculator.personItemsTotal(items, bob)
        assertEquals(2500L, aliceItems)
        assertEquals(1700L, bobItems)

        val aliceTip = SplitCalculator.personTipShare(personIndex = 0, peopleCount = 2, tipCents = tip)
        val bobTip = SplitCalculator.personTipShare(personIndex = 1, peopleCount = 2, tipCents = tip)
        assertEquals(210L, aliceTip)
        assertEquals(210L, bobTip)

        // Everything adds back up to the grand total, exactly.
        assertEquals(subtotal + tip, aliceItems + aliceTip + bobItems + bobTip)
        assertEquals("$27.10", SplitCalculator.formatMoney(aliceItems + aliceTip, "$"))
        assertEquals("$19.10", SplitCalculator.formatMoney(bobItems + bobTip, "$"))
    }

    @Test
    fun `money formats cents with two decimals`() {
        assertEquals("$0.00", SplitCalculator.formatMoney(0L, "$"))
        assertEquals("$10.00", SplitCalculator.formatMoney(1000L, "$"))
        assertEquals("$3.33", SplitCalculator.formatMoney(333L, "$"))
        assertEquals("$0.05", SplitCalculator.formatMoney(5L, "$"))
        assertEquals("R$12.34", SplitCalculator.formatMoney(1234L, "R$"))
    }

    @Test
    fun `price parsing accepts dot and comma decimals`() {
        assertEquals(1250L, SplitCalculator.parsePriceCents("12.50"))
        assertEquals(1250L, SplitCalculator.parsePriceCents("12,5"))
        assertEquals(1000L, SplitCalculator.parsePriceCents(" 10 "))
        // sub-cent digits round half-up
        assertEquals(1235L, SplitCalculator.parsePriceCents("12.345"))
        assertNull(SplitCalculator.parsePriceCents(""))
        assertNull(SplitCalculator.parsePriceCents("abc"))
    }

    @Test
    fun `price edit formatting strips trailing zeros`() {
        assertEquals("", SplitCalculator.formatPriceForEdit(0L))
        assertEquals("10", SplitCalculator.formatPriceForEdit(1000L))
        assertEquals("12.5", SplitCalculator.formatPriceForEdit(1250L))
        assertEquals("12.34", SplitCalculator.formatPriceForEdit(1234L))
    }

    @Test
    fun `initials from first and second name`() {
        assertEquals("AS", SplitCalculator.initialsFor("ana silva"))
        assertEquals("B", SplitCalculator.initialsFor("Bob"))
        assertEquals("PH", SplitCalculator.initialsFor("  pedro   henrique flores "))
        assertEquals("?", SplitCalculator.initialsFor("   "))
    }
}
