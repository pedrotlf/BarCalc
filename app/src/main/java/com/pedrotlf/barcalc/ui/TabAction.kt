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

    /** Ask to start over — opens the confirmation dialog (see [Reset]). */
    data object RequestReset : TabAction

    /** Dismiss the reset confirmation without wiping anything. */
    data object DismissReset : TabAction

    /** Confirmed: wipe the tab and start fresh. */
    data object Reset : TabAction

    // ── About ──────────────────────────────────────────────────────────────
    data object ShowAbout : TabAction
    data object HideAbout : TabAction

    // ── Scanning a tab ─────────────────────────────────────────────────────
    /**
     * A photo of the tab came back from the scanner. [imageUri] is read once
     * and not kept — the picture never becomes part of the session.
     */
    data class ReceiptCaptured(val imageUri: String) : TabAction

    /** The scanner was dismissed or couldn't start. */
    data object ScanCancelled : TabAction

    /** Close the review, discarding everything that was scanned. */
    data object DismissScanResult : TabAction

    // ── Checking over a scan ───────────────────────────────────────────────
    /** Keep or drop a scanned line. */
    data class ToggleScanDraft(val id: Int) : TabAction
    data class ScanDraftNameChanged(val id: Int, val name: String) : TabAction
    data class ScanDraftPriceChanged(val id: Int, val cents: Long) : TabAction
    data class IncScanDraftQty(val id: Int) : TabAction
    data class DecScanDraftQty(val id: Int) : TabAction

    /** Add the kept lines to the tab and close the review. */
    data object ConfirmScannedItems : TabAction

    // ── Drawer ─────────────────────────────────────────────────────────────
    data object OpenDrawer : TabAction
    data object CloseDrawer : TabAction

    // ── History ────────────────────────────────────────────────────────────
    data object ShowHistory : TabAction
    data object HideHistory : TabAction

    /**
     * Tapped a history entry. Opens the replace warning when a tab is already
     * in progress, otherwise duplicates straight away.
     */
    data class RequestDuplicate(val entryId: Long) : TabAction

    /** Confirmed the warning: replace the current tab with the duplicate. */
    data object ConfirmDuplicate : TabAction
    data object DismissDuplicate : TabAction

    /** Open/close the rename prompt for a history entry. */
    data class RequestRename(val entryId: Long) : TabAction
    data object DismissRename : TabAction
    data class RenameDraftChanged(val value: String) : TabAction
    data object ConfirmRename : TabAction

    /** Delete a single entry, with confirmation. */
    data class RequestDeleteEntry(val entryId: Long) : TabAction
    data object ConfirmDeleteEntry : TabAction
    data object DismissDeleteEntry : TabAction

    /** Wipe the whole history, with confirmation. */
    data object RequestClearHistory : TabAction
    data object ConfirmClearHistory : TabAction
    data object DismissClearHistory : TabAction
}

/**
 * Actions that move the user away from whatever they were typing in — opening
 * a sheet or menu, or stepping to another screen. The keyboard is dismissed
 * for these before they run, so it can't sit over the thing being opened.
 *
 * Listed here beside the actions themselves so a new destination has to make
 * the same decision, rather than the rule being buried in call sites.
 */
val TabAction.dismissesKeyboard: Boolean
    get() = when (this) {
        // Navigation between wizard screens.
        TabAction.GoToPeople,
        TabAction.GoToResults,
        TabAction.Back,
        // Menus and full-screen destinations.
        TabAction.OpenDrawer,
        TabAction.ShowHistory,
        TabAction.ShowAbout,
        // Sheets and dialogs that cover the screen.
        TabAction.RequestReset,
        TabAction.RequestClearHistory,
        // Closes the review and lands back on the items screen.
        TabAction.ConfirmScannedItems,
        is TabAction.OpenPerson,
        is TabAction.RequestDuplicate,
        is TabAction.RequestRename,
        is TabAction.RequestDeleteEntry,
        -> true

        else -> false
    }
