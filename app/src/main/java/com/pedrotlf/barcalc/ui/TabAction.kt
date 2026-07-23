package com.pedrotlf.barcalc.ui

/**
 * Everything the UI can ask the [TabViewModel] to do. Screens receive
 * `state + onAction` instead of the ViewModel itself, so they stay pure,
 * previewable, and testable.
 */
sealed interface TabAction {

    // ── Items screen ───────────────────────────────────────────────────────
    data class NewItemNameChanged(val value: String) : TabAction
    data class NewItemPriceChanged(val cents: Long) : TabAction
    data object IncNewQty : TabAction
    data object DecNewQty : TabAction
    data object AddItem : TabAction
    data class ItemNameChanged(val id: Int, val name: String) : TabAction
    data class ItemPriceChanged(val id: Int, val cents: Long) : TabAction
    data class IncItemQty(val id: Int) : TabAction
    data class DecItemQty(val id: Int) : TabAction
    data class RemoveItem(val id: Int) : TabAction

    // ── People screen ──────────────────────────────────────────────────────
    data class NewPersonNameChanged(val value: String) : TabAction
    data object AddPerson : TabAction
    data class RemovePerson(val id: Int) : TabAction
    data class OpenPerson(val id: Int) : TabAction
    data object CloseSheet : TabAction
    data class ToggleUnitClaim(val itemId: Int, val unitIndex: Int, val personId: Int) : TabAction
    data class SetAllUnitsClaim(val itemId: Int, val personId: Int, val claimed: Boolean) : TabAction
    data object ToggleTip : TabAction
    data object IncTip : TabAction
    data object DecTip : TabAction

    // ── Navigation ─────────────────────────────────────────────────────────
    data object GoToPeople : TabAction
    data object GoToResults : TabAction
    data object Back : TabAction

    // ── Results screen ─────────────────────────────────────────────────────
    data class ToggleExpand(val personId: Int) : TabAction
    data object Reset : TabAction

    // ── About ──────────────────────────────────────────────────────────────
    data object ShowAbout : TabAction
    data object HideAbout : TabAction
}
