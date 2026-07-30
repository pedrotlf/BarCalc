package com.pedrotlf.barcalc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrotlf.barcalc.data.SessionRepository
import com.pedrotlf.barcalc.data.history.HistoryStore
import com.pedrotlf.barcalc.domain.Person
import com.pedrotlf.barcalc.domain.SplitCalculator
import com.pedrotlf.barcalc.domain.TabItem
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Single source of truth for the whole wizard. The UI talks to it through
 * [onAction]; the named methods stay public for direct use in tests.
 * [repository] is null in unit tests (no persistence).
 */
class TabViewModel(
    private val repository: SessionRepository? = null,
    private val history: HistoryStore? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TabUiState())
    val uiState: StateFlow<TabUiState> = _uiState.asStateFlow()

    init {
        if (repository != null) restoreThenAutoSave(repository)
        if (history != null) observeHistory(history)
    }

    private fun observeHistory(store: HistoryStore) {
        viewModelScope.launch {
            store.observeEntries().collect { entries ->
                _uiState.update { it.copy(history = entries) }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun restoreThenAutoSave(repo: SessionRepository) {
        viewModelScope.launch {
            repo.load()?.let { saved ->
                _uiState.update { it.copy(session = saved) }
            }
            // Debounced auto-save of every session change after restore.
            uiState
                .map { it.session }
                .distinctUntilChanged()
                .drop(1)
                .debounce(SAVE_DEBOUNCE_MS)
                .collect { repo.save(it) }
        }
    }

    /** Single entry point for the UI. */
    fun onAction(action: TabAction) {
        when (action) {
            is TabAction.NewItemNameChanged -> onNewItemNameChange(action.value)
            is TabAction.NewItemPriceChanged -> onNewItemPriceChange(action.cents)
            TabAction.IncNewQty -> incNewQty()
            TabAction.DecNewQty -> decNewQty()
            TabAction.AddItem -> addItem()
            is TabAction.ItemNameChanged -> updateItemName(action.id, action.name)
            is TabAction.ItemPriceChanged -> updateItemPrice(action.id, action.cents)
            is TabAction.IncItemQty -> incItemQty(action.id)
            is TabAction.DecItemQty -> decItemQty(action.id)
            is TabAction.RemoveItem -> removeItem(action.id)

            is TabAction.NewPersonNameChanged -> onNewPersonNameChange(action.value)
            TabAction.AddPerson -> addPerson()
            is TabAction.RemovePerson -> removePerson(action.id)
            is TabAction.OpenPerson -> openPerson(action.id)
            TabAction.CloseSheet -> closeSheet()
            is TabAction.ToggleUnitClaim ->
                toggleUnitClaim(action.itemId, action.unitIndex, action.personId)
            is TabAction.SetAllUnitsClaim ->
                setAllUnitsClaim(action.itemId, action.personId, action.claimed)
            TabAction.ToggleTip -> toggleTip()
            TabAction.IncTip -> incTip()
            TabAction.DecTip -> decTip()

            TabAction.GoToPeople -> goToPeople()
            TabAction.GoToResults -> goToResults()
            TabAction.Back -> goBack()

            is TabAction.ToggleExpand -> toggleExpand(action.personId)
            TabAction.RequestReset -> _uiState.update { it.copy(showResetConfirm = true) }
            TabAction.DismissReset -> _uiState.update { it.copy(showResetConfirm = false) }
            TabAction.Reset -> reset()

            TabAction.RequestClearTab -> _uiState.update { it.copy(showClearTabConfirm = true) }
            TabAction.DismissClearTab -> _uiState.update { it.copy(showClearTabConfirm = false) }
            TabAction.ClearTab -> clearTab()

            TabAction.ShowAbout -> _uiState.update { it.copy(showAbout = true, showDrawer = false) }
            TabAction.HideAbout -> _uiState.update { it.copy(showAbout = false) }

            TabAction.OpenDrawer -> _uiState.update { it.copy(showDrawer = true) }
            TabAction.CloseDrawer -> _uiState.update { it.copy(showDrawer = false) }

            TabAction.ShowHistory ->
                _uiState.update { it.copy(showHistory = true, showDrawer = false) }
            TabAction.HideHistory -> _uiState.update { it.copy(showHistory = false) }

            is TabAction.RequestDuplicate -> requestDuplicate(action.entryId)
            TabAction.ConfirmDuplicate -> confirmDuplicate()
            TabAction.DismissDuplicate -> _uiState.update { it.copy(pendingDuplicateId = null) }

            is TabAction.RequestRename -> startRename(action.entryId)
            TabAction.DismissRename ->
                _uiState.update { it.copy(renamingEntryId = null, renameDraft = "") }
            is TabAction.RenameDraftChanged ->
                _uiState.update { it.copy(renameDraft = action.value) }
            TabAction.ConfirmRename -> confirmRename()

            is TabAction.RequestDeleteEntry ->
                _uiState.update { it.copy(pendingDeleteEntryId = action.entryId) }
            TabAction.ConfirmDeleteEntry -> confirmDeleteEntry()
            TabAction.DismissDeleteEntry ->
                _uiState.update { it.copy(pendingDeleteEntryId = null) }

            TabAction.RequestClearHistory ->
                _uiState.update { it.copy(showClearHistoryConfirm = true) }
            TabAction.ConfirmClearHistory -> confirmClearHistory()
            TabAction.DismissClearHistory ->
                _uiState.update { it.copy(showClearHistoryConfirm = false) }
        }
    }

    // ── History ────────────────────────────────────────────────────────────

    /**
     * Duplicating replaces the working tab, so warn first when that would
     * throw away real work; otherwise go straight through.
     */
    private fun requestDuplicate(entryId: Long) {
        if (_uiState.value.hasWorkInProgress) {
            _uiState.update { it.copy(pendingDuplicateId = entryId) }
        } else {
            duplicate(entryId)
        }
    }

    private fun confirmDuplicate() {
        val id = _uiState.value.pendingDuplicateId ?: return
        _uiState.update { it.copy(pendingDuplicateId = null) }
        duplicate(id)
    }

    /**
     * Loads the archived session into a fresh working tab — an exact copy,
     * claims and tip included. The archived entry itself is left untouched.
     */
    private fun duplicate(entryId: Long) {
        val store = history ?: return
        viewModelScope.launch {
            val session = store.loadSession(entryId) ?: return@launch
            _uiState.update {
                TabUiState(session = session, history = it.history)
            }
        }
    }

    private fun startRename(entryId: Long) {
        val current = _uiState.value.history.firstOrNull { it.id == entryId }
        _uiState.update {
            it.copy(renamingEntryId = entryId, renameDraft = current?.customName.orEmpty())
        }
    }

    private fun confirmRename() {
        val state = _uiState.value
        val id = state.renamingEntryId ?: return
        val name = state.renameDraft.trim().takeIf { it.isNotEmpty() }
        _uiState.update { it.copy(renamingEntryId = null, renameDraft = "") }
        history?.let { store -> viewModelScope.launch { store.rename(id, name) } }
    }

    private fun confirmDeleteEntry() {
        val id = _uiState.value.pendingDeleteEntryId ?: return
        _uiState.update { it.copy(pendingDeleteEntryId = null) }
        history?.let { store -> viewModelScope.launch { store.delete(id) } }
    }

    private fun confirmClearHistory() {
        _uiState.update { it.copy(showClearHistoryConfirm = false) }
        history?.let { store -> viewModelScope.launch { store.clearAll() } }
    }

    private inline fun updateSession(crossinline block: (TabSession) -> TabSession) {
        _uiState.update { it.copy(session = block(it.session)) }
    }

    private inline fun updateItems(crossinline block: (List<TabItem>) -> List<TabItem>) {
        updateSession { it.copy(items = block(it.items)) }
    }

    // ── Items screen ───────────────────────────────────────────────────────

    fun onNewItemNameChange(value: String) = _uiState.update { it.copy(newItemName = value) }

    fun onNewItemPriceChange(cents: Long) = _uiState.update { it.copy(newItemPriceCents = cents) }

    fun incNewQty() = _uiState.update {
        it.copy(newItemQty = (it.newItemQty + 1).coerceAtMost(TabDefaults.QTY_MAX))
    }

    fun decNewQty() = _uiState.update {
        it.copy(newItemQty = (it.newItemQty - 1).coerceAtLeast(1))
    }

    fun addItem() {
        val state = _uiState.value
        val name = state.newItemName.trim()
        val priceCents = state.newItemPriceCents
        if (name.isEmpty() || priceCents <= 0L) return
        _uiState.update {
            it.copy(
                session = it.session.copy(
                    items = it.session.items +
                        TabItem.new(it.session.itemSeq, name, priceCents, it.newItemQty),
                    itemSeq = it.session.itemSeq + 1,
                ),
                newItemName = "",
                newItemPriceCents = 0L,
                newItemQty = 1,
            )
        }
    }

    fun updateItemName(id: Int, name: String) =
        updateItems { items -> items.map { if (it.id == id) it.copy(name = name) else it } }

    fun updateItemPrice(id: Int, cents: Long) = updateItems { items ->
        items.map { if (it.id == id) it.copy(priceCents = cents) else it }
    }

    fun incItemQty(id: Int) = updateItems { items ->
        items.map { if (it.id == id) it.withQtyIncremented() else it }
    }

    fun decItemQty(id: Int) = updateItems { items ->
        items.map { if (it.id == id) it.withQtyDecremented() else it }
    }

    fun removeItem(id: Int) = updateItems { items -> items.filter { it.id != id } }

    // ── People screen ──────────────────────────────────────────────────────

    fun onNewPersonNameChange(value: String) = _uiState.update { it.copy(newPersonName = value) }

    fun addPerson() {
        val name = _uiState.value.newPersonName.trim()
        if (name.isEmpty()) return
        _uiState.update {
            it.copy(
                session = it.session.copy(
                    people = it.session.people + Person(it.session.personSeq, name),
                    personSeq = it.session.personSeq + 1,
                ),
                newPersonName = "",
            )
        }
    }

    fun removePerson(id: Int) {
        _uiState.update {
            it.copy(
                session = it.session.copy(
                    people = it.session.people.filter { p -> p.id != id },
                    items = it.session.items.map { item -> item.withPersonRemoved(id) },
                ),
                activePersonId = if (it.activePersonId == id) null else it.activePersonId,
            )
        }
    }

    fun openPerson(id: Int) = _uiState.update { it.copy(activePersonId = id) }

    fun closeSheet() = _uiState.update { it.copy(activePersonId = null) }

    fun toggleUnitClaim(itemId: Int, unitIndex: Int, personId: Int) = updateItems { items ->
        items.map { if (it.id == itemId) it.withClaimToggled(unitIndex, personId) else it }
    }

    fun setAllUnitsClaim(itemId: Int, personId: Int, claimed: Boolean) = updateItems { items ->
        items.map { if (it.id == itemId) it.withAllUnitsClaimed(personId, claimed) else it }
    }

    // ── Tip ────────────────────────────────────────────────────────────────

    fun toggleTip() = updateSession { it.copy(tipEnabled = !it.tipEnabled) }

    fun incTip() = updateSession {
        it.copy(tipPercent = (it.tipPercent + 1).coerceAtMost(TabDefaults.TIP_PERCENT_MAX))
    }

    fun decTip() = updateSession {
        it.copy(tipPercent = (it.tipPercent - 1).coerceAtLeast(0))
    }

    // ── Navigation ─────────────────────────────────────────────────────────

    fun goToPeople() {
        if (_uiState.value.items.isNotEmpty()) updateSession { it.copy(screen = Screen.PEOPLE) }
    }

    fun goToResults() {
        val s = _uiState.value.session
        if (s.people.isEmpty() || SplitCalculator.unclaimedItems(s.items).isNotEmpty()) return
        updateSession { it.copy(screen = Screen.RESULTS) }
    }

    /**
     * System back: close the claim sheet if open, otherwise step the wizard
     * back. Returns false on the first screen so the activity can finish.
     */
    fun goBack(): Boolean {
        val state = _uiState.value
        return when {
            // Innermost overlays first, outwards.
            state.renamingEntryId != null -> {
                _uiState.update { it.copy(renamingEntryId = null, renameDraft = "") }; true
            }
            state.pendingDuplicateId != null -> {
                _uiState.update { it.copy(pendingDuplicateId = null) }; true
            }
            state.pendingDeleteEntryId != null -> {
                _uiState.update { it.copy(pendingDeleteEntryId = null) }; true
            }
            state.showClearHistoryConfirm -> {
                _uiState.update { it.copy(showClearHistoryConfirm = false) }; true
            }
            state.showAbout -> { _uiState.update { it.copy(showAbout = false) }; true }
            state.showHistory -> { _uiState.update { it.copy(showHistory = false) }; true }
            state.showDrawer -> { _uiState.update { it.copy(showDrawer = false) }; true }
            state.showResetConfirm -> { _uiState.update { it.copy(showResetConfirm = false) }; true }
            state.showClearTabConfirm -> {
                _uiState.update { it.copy(showClearTabConfirm = false) }; true
            }
            state.activePersonId != null -> { closeSheet(); true }
            state.screen == Screen.RESULTS -> { updateSession { it.copy(screen = Screen.PEOPLE) }; true }
            state.screen == Screen.PEOPLE -> { updateSession { it.copy(screen = Screen.ITEMS) }; true }
            else -> false
        }
    }

    // ── Results ────────────────────────────────────────────────────────────

    fun toggleExpand(personId: Int) = _uiState.update {
        val expanded = it.expandedResultIds
        it.copy(
            expandedResultIds = if (personId in expanded) expanded - personId else expanded + personId
        )
    }

    /**
     * Finish the tab: archive it to history first (so nothing is ever lost),
     * then clear the working session.
     */
    fun reset() = wipeTab(archive = true)

    /**
     * Discard the tab outright — same wipe as [reset] but nothing is kept.
     * For a tab entered by mistake, which would only clutter the history.
     */
    fun clearTab() = wipeTab(archive = false)

    /** Empty tabs aren't worth archiving even when [archive] is set. */
    private fun wipeTab(archive: Boolean) {
        val discarded = _uiState.value.session
        _uiState.update { TabUiState(history = it.history) }
        viewModelScope.launch {
            if (archive && discarded.items.isNotEmpty()) history?.archive(discarded)
            repository?.clear()
        }
    }

    private companion object {
        const val SAVE_DEBOUNCE_MS = 400L
    }
}
