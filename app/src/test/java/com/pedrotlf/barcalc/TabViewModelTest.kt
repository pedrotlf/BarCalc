package com.pedrotlf.barcalc

import com.pedrotlf.barcalc.ui.Screen
import com.pedrotlf.barcalc.ui.TabViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TabViewModelTest {

    @Test
    fun `full happy path from items to results`() {
        val vm = TabViewModel()

        // Screen 1 — guard: can't advance with no items
        vm.goToPeople()
        assertEquals(Screen.ITEMS, vm.uiState.value.screen)

        // Add "Beer" x3 at $10
        vm.onNewItemNameChange("Beer")
        vm.onNewItemPriceChange("10")
        vm.incNewQty()
        vm.incNewQty()
        assertTrue(vm.uiState.value.addItemEnabled)
        vm.addItem()

        // Add "Nachos" x1 at $12 (comma decimal accepted)
        vm.onNewItemNameChange("Nachos")
        vm.onNewItemPriceChange("12,00")
        vm.addItem()

        val items = vm.uiState.value.items
        assertEquals(2, items.size)
        assertEquals(3, items[0].qty)
        assertEquals(1200L, items[1].priceCents)
        // Drafts cleared
        assertEquals("", vm.uiState.value.newItemName)
        assertEquals(1, vm.uiState.value.newItemQty)

        vm.goToPeople()
        assertEquals(Screen.PEOPLE, vm.uiState.value.screen)

        // Screen 2 — add Alice & Bob
        vm.onNewPersonNameChange("Alice")
        vm.addPerson()
        vm.onNewPersonNameChange("Bob")
        vm.addPerson()
        val (alice, bob) = vm.uiState.value.people

        // Guard: unclaimed items block results
        vm.goToResults()
        assertEquals(Screen.PEOPLE, vm.uiState.value.screen)

        // Alice claims beer units 0,1 and shares 2 with Bob; Bob takes nachos
        val beerId = items[0].id
        val nachosId = items[1].id
        vm.toggleUnitClaim(beerId, 0, alice.id)
        vm.toggleUnitClaim(beerId, 1, alice.id)
        vm.toggleUnitClaim(beerId, 2, alice.id)
        vm.toggleUnitClaim(beerId, 2, bob.id)
        vm.toggleUnitClaim(nachosId, 0, bob.id)

        vm.goToResults()
        assertEquals(Screen.RESULTS, vm.uiState.value.screen)

        // Back steps the wizard backwards
        assertTrue(vm.goBack())
        assertEquals(Screen.PEOPLE, vm.uiState.value.screen)
        vm.openPerson(alice.id)
        assertTrue(vm.goBack()) // closes sheet first
        assertEquals(Screen.PEOPLE, vm.uiState.value.screen)
        assertTrue(vm.goBack())
        assertEquals(Screen.ITEMS, vm.uiState.value.screen)
        assertFalse(vm.goBack()) // first screen: let the activity finish

        // Reset clears everything
        vm.reset()
        assertTrue(vm.uiState.value.items.isEmpty())
        assertTrue(vm.uiState.value.people.isEmpty())
        assertEquals(Screen.ITEMS, vm.uiState.value.screen)
    }

    @Test
    fun `removing a person clears their claims and closes their sheet`() {
        val vm = TabViewModel()
        vm.onNewItemNameChange("Beer")
        vm.onNewItemPriceChange("10")
        vm.addItem()
        vm.onNewPersonNameChange("Alice")
        vm.addPerson()
        val alice = vm.uiState.value.people.single()
        val beer = vm.uiState.value.items.single()

        vm.toggleUnitClaim(beer.id, 0, alice.id)
        vm.openPerson(alice.id)
        vm.removePerson(alice.id)

        assertTrue(vm.uiState.value.people.isEmpty())
        assertTrue(vm.uiState.value.items.single().units.all { it.isEmpty() })
        assertEquals(null, vm.uiState.value.activePersonId)
    }

    @Test
    fun `tip stepper stays within 0-40`() {
        val vm = TabViewModel()
        repeat(50) { vm.incTip() }
        assertEquals(40, vm.uiState.value.tipPercent)
        repeat(50) { vm.decTip() }
        assertEquals(0, vm.uiState.value.tipPercent)
        vm.toggleTip()
        assertFalse(vm.uiState.value.tipEnabled)
    }
}
