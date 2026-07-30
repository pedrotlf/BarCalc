package com.pedrotlf.barcalc

import com.pedrotlf.barcalc.data.history.HistoryStore
import com.pedrotlf.barcalc.domain.HistoryEntry
import com.pedrotlf.barcalc.ui.Screen
import com.pedrotlf.barcalc.ui.TabAction
import com.pedrotlf.barcalc.ui.TabSession
import com.pedrotlf.barcalc.ui.TabViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** In-memory [HistoryStore] so the view model can be tested without Room. */
private class FakeHistoryStore : HistoryStore {
    val entries = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val archived = mutableListOf<TabSession>()
    var cleared = false
    private var nextId = 1L

    override fun observeEntries(): Flow<List<HistoryEntry>> = entries

    override suspend fun archive(session: TabSession): Long {
        archived += session
        val id = nextId++
        entries.value = entries.value + HistoryEntry(
            id = id,
            savedAt = id,
            customName = null,
            itemCount = session.items.sumOf { it.qty },
            personCount = session.people.size,
            totalCents = session.items.sumOf { it.priceCents * it.qty },
        )
        return id
    }

    override suspend fun loadSession(id: Long): TabSession? =
        archived.getOrNull((id - 1).toInt())

    override suspend fun rename(id: Long, name: String?) {
        entries.value = entries.value.map { if (it.id == id) it.copy(customName = name) else it }
    }

    override suspend fun delete(id: Long) {
        entries.value = entries.value.filterNot { it.id == id }
    }

    override suspend fun clearAll() {
        cleared = true
        entries.value = emptyList()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TabViewModelTest {

    // viewModelScope posts to Dispatchers.Main, which has no Android looper here.
    @Before
    fun setUpDispatcher() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDownDispatcher() = Dispatchers.resetMain()

    @Test
    fun `full happy path from items to results`() {
        val vm = TabViewModel()

        // Screen 1 — guard: can't advance with no items
        vm.goToPeople()
        assertEquals(Screen.ITEMS, vm.uiState.value.screen)

        // Add "Beer" x3 at $10.00
        vm.onNewItemNameChange("Beer")
        vm.onNewItemPriceChange(1000L)
        vm.incNewQty()
        vm.incNewQty()
        assertTrue(vm.uiState.value.addItemEnabled)
        vm.addItem()

        // Add "Nachos" x1 at $12.00
        vm.onNewItemNameChange("Nachos")
        vm.onNewItemPriceChange(1200L)
        vm.addItem()

        val items = vm.uiState.value.items
        assertEquals(2, items.size)
        assertEquals(3, items[0].qty)
        assertEquals(1200L, items[1].priceCents)
        // Drafts cleared
        assertEquals("", vm.uiState.value.newItemName)
        assertEquals(0L, vm.uiState.value.newItemPriceCents)
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
        vm.onNewItemPriceChange(1000L)
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
    fun `reset is confirmed before it wipes the tab`() {
        val vm = TabViewModel()
        vm.onNewItemNameChange("Beer")
        vm.onNewItemPriceChange(1000L)
        vm.addItem()

        // Requesting reset only opens the confirmation — data stays intact.
        vm.onAction(TabAction.RequestReset)
        assertTrue(vm.uiState.value.showResetConfirm)
        assertTrue(vm.uiState.value.items.isNotEmpty())

        // Dismissing (or system back) closes it without wiping.
        vm.onAction(TabAction.DismissReset)
        assertFalse(vm.uiState.value.showResetConfirm)
        assertTrue(vm.uiState.value.items.isNotEmpty())

        vm.onAction(TabAction.RequestReset)
        assertTrue(vm.goBack())
        assertFalse(vm.uiState.value.showResetConfirm)
        assertTrue(vm.uiState.value.items.isNotEmpty())

        // Only a confirmed reset clears the tab.
        vm.onAction(TabAction.RequestReset)
        vm.onAction(TabAction.Reset)
        assertFalse(vm.uiState.value.showResetConfirm)
        assertTrue(vm.uiState.value.items.isEmpty())
    }

    @Test
    fun `finishing a tab archives it to history`() {
        val store = FakeHistoryStore()
        val vm = TabViewModel(history = store)
        vm.onNewItemNameChange("Beer")
        vm.onNewItemPriceChange(1000L)
        vm.addItem()
        vm.onNewPersonNameChange("Alice")
        vm.addPerson()

        vm.onAction(TabAction.RequestReset)
        vm.onAction(TabAction.Reset)

        // Working tab is cleared, but the finished one is preserved.
        assertTrue(vm.uiState.value.items.isEmpty())
        assertEquals(1, store.archived.size)
        assertEquals("Beer", store.archived.single().items.single().name)
        assertEquals(1, vm.uiState.value.history.size)
    }

    @Test
    fun `clearing the tab discards it instead of archiving it`() {
        val store = FakeHistoryStore()
        val vm = TabViewModel(history = store)

        // An earlier tab, properly finished, so we can prove clearing later
        // wipes only the working tab and not the history behind it.
        vm.onNewItemNameChange("Nachos")
        vm.onNewItemPriceChange(1200L)
        vm.addItem()
        vm.onAction(TabAction.Reset)
        assertEquals(1, vm.uiState.value.history.size)

        vm.onNewItemNameChange("Beer")
        vm.onNewItemPriceChange(1000L)
        vm.addItem()
        vm.onNewPersonNameChange("Alice")
        vm.addPerson()

        // Requesting only opens the confirmation — data stays intact.
        vm.onAction(TabAction.RequestClearTab)
        assertTrue(vm.uiState.value.showClearTabConfirm)
        assertEquals(1, vm.uiState.value.items.size)

        // Dismissing — by button or by system back — leaves the tab intact.
        vm.onAction(TabAction.DismissClearTab)
        assertFalse(vm.uiState.value.showClearTabConfirm)
        vm.onAction(TabAction.RequestClearTab)
        assertTrue(vm.goBack())
        assertFalse(vm.uiState.value.showClearTabConfirm)
        assertEquals(1, vm.uiState.value.items.size)

        // Only a confirmed clear wipes it, and it is not kept anywhere.
        vm.onAction(TabAction.RequestClearTab)
        vm.onAction(TabAction.ClearTab)
        assertTrue(vm.uiState.value.items.isEmpty())
        assertTrue(vm.uiState.value.people.isEmpty())
        assertFalse(vm.uiState.value.hasWorkInProgress)
        assertEquals(listOf("Nachos"), store.archived.map { it.items.single().name })
        assertEquals(1, vm.uiState.value.history.size)
    }

    @Test
    fun `an empty tab is not archived`() {
        val store = FakeHistoryStore()
        val vm = TabViewModel(history = store)
        vm.onAction(TabAction.Reset)
        assertTrue(store.archived.isEmpty())
    }

    @Test
    fun `duplicating warns first only when a tab is in progress`() {
        val store = FakeHistoryStore()
        val vm = TabViewModel(history = store)

        // Archive a tab with a claim, then finish it.
        vm.onNewItemNameChange("Beer")
        vm.onNewItemPriceChange(1000L)
        vm.addItem()
        vm.onNewPersonNameChange("Alice")
        vm.addPerson()
        val alice = vm.uiState.value.people.single()
        vm.toggleUnitClaim(vm.uiState.value.items.single().id, 0, alice.id)
        vm.onAction(TabAction.Reset)
        val entryId = vm.uiState.value.history.single().id

        // Nothing in progress: duplicates immediately, claims included.
        vm.onAction(TabAction.RequestDuplicate(entryId))
        assertNull(vm.uiState.value.pendingDuplicateId)
        assertEquals("Beer", vm.uiState.value.items.single().name)
        assertEquals(listOf(alice.id), vm.uiState.value.items.single().units[0])

        // Now something is in progress, so it must warn before replacing.
        vm.onNewItemNameChange("Nachos")
        vm.onNewItemPriceChange(500L)
        vm.addItem()
        vm.onAction(TabAction.RequestDuplicate(entryId))
        assertEquals(entryId, vm.uiState.value.pendingDuplicateId)
        assertEquals(2, vm.uiState.value.items.size) // untouched until confirmed

        vm.onAction(TabAction.DismissDuplicate)
        assertNull(vm.uiState.value.pendingDuplicateId)
        assertEquals(2, vm.uiState.value.items.size)

        vm.onAction(TabAction.RequestDuplicate(entryId))
        vm.onAction(TabAction.ConfirmDuplicate)
        assertEquals(1, vm.uiState.value.items.size)
        assertEquals("Beer", vm.uiState.value.items.single().name)
    }

    @Test
    fun `history entries can be renamed and deleted`() {
        val store = FakeHistoryStore()
        val vm = TabViewModel(history = store)
        vm.onNewItemNameChange("Beer")
        vm.onNewItemPriceChange(1000L)
        vm.addItem()
        vm.onAction(TabAction.Reset)
        val entryId = vm.uiState.value.history.single().id

        vm.onAction(TabAction.RequestRename(entryId))
        vm.onAction(TabAction.RenameDraftChanged("Friday"))
        vm.onAction(TabAction.ConfirmRename)
        assertEquals("Friday", vm.uiState.value.history.single().customName)
        assertNull(vm.uiState.value.renamingEntryId)

        // Blanking the name falls back to the generated summary.
        vm.onAction(TabAction.RequestRename(entryId))
        vm.onAction(TabAction.RenameDraftChanged("   "))
        vm.onAction(TabAction.ConfirmRename)
        assertNull(vm.uiState.value.history.single().customName)

        vm.onAction(TabAction.RequestDeleteEntry(entryId))
        assertEquals(1, vm.uiState.value.history.size) // not yet
        vm.onAction(TabAction.ConfirmDeleteEntry)
        assertTrue(vm.uiState.value.history.isEmpty())
    }

    @Test
    fun `back closes history overlays innermost first`() {
        val store = FakeHistoryStore()
        val vm = TabViewModel(history = store)

        vm.onAction(TabAction.OpenDrawer)
        vm.onAction(TabAction.ShowHistory)
        assertFalse(vm.uiState.value.showDrawer) // opening history closes the drawer
        assertTrue(vm.uiState.value.showHistory)

        vm.onAction(TabAction.RequestClearHistory)
        assertTrue(vm.goBack())
        assertFalse(vm.uiState.value.showClearHistoryConfirm)
        assertTrue(vm.uiState.value.showHistory) // history stays open

        assertTrue(vm.goBack())
        assertFalse(vm.uiState.value.showHistory)
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
