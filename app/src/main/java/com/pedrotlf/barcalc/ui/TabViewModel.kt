package com.pedrotlf.barcalc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedrotlf.barcalc.data.SessionRepository
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(TabUiState())
    val uiState: StateFlow<TabUiState> = _uiState.asStateFlow()

    init {
        if (repository != null) restoreThenAutoSave(repository)
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
            TabAction.Reset -> reset()

            TabAction.ShowAbout -> _uiState.update { it.copy(showAbout = true) }
            TabAction.HideAbout -> _uiState.update { it.copy(showAbout = false) }
        }
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
            state.showAbout -> { _uiState.update { it.copy(showAbout = false) }; true }
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

    fun reset() {
        _uiState.value = TabUiState()
        repository?.let { repo -> viewModelScope.launch { repo.clear() } }
    }

    private companion object {
        const val SAVE_DEBOUNCE_MS = 400L
    }
}
