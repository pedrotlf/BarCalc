package com.pedrotlf.barcalc

import com.pedrotlf.barcalc.ui.TabAction
import com.pedrotlf.barcalc.ui.dismissesKeyboard
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Which actions take the keyboard down on their way out. */
class TabActionKeyboardTest {

    @Test
    fun `leaving the screen or opening something over it dismisses the keyboard`() {
        val actions = listOf(
            TabAction.GoToPeople,
            TabAction.GoToResults,
            TabAction.Back,
            TabAction.OpenDrawer,
            TabAction.ShowHistory,
            TabAction.ShowAbout,
            TabAction.RequestReset,
            TabAction.RequestClearTab,
            TabAction.RequestClearHistory,
            TabAction.OpenPerson(1),
            TabAction.RequestDuplicate(1L),
            TabAction.RequestRename(1L),
            TabAction.RequestDeleteEntry(1L),
        )
        actions.forEach { assertTrue("$it should dismiss the keyboard", it.dismissesKeyboard) }
    }

    @Test
    fun `editing in place leaves the keyboard up`() {
        // Typing, stepping quantities and claiming units all happen while a
        // field may be focused — closing the keyboard there would fight the user.
        val actions = listOf(
            TabAction.NewItemNameChanged("Beer"),
            TabAction.NewItemPriceChanged(1000L),
            TabAction.AddItem,
            TabAction.IncNewQty,
            TabAction.ItemNameChanged(1, "Beer"),
            TabAction.ItemPriceChanged(1, 500L),
            TabAction.NewPersonNameChanged("Alice"),
            TabAction.AddPerson,
            TabAction.ToggleUnitClaim(1, 0, 1),
            TabAction.ToggleTip,
            TabAction.RenameDraftChanged("Friday"),
        )
        actions.forEach { assertFalse("$it should keep the keyboard", it.dismissesKeyboard) }
    }

    @Test
    fun `dismissing an overlay leaves the keyboard alone`() {
        // These close things rather than open them, so there is nothing to
        // uncover — and the rename dialog's own field must keep its keyboard.
        val actions = listOf(
            TabAction.CloseSheet,
            TabAction.HideAbout,
            TabAction.HideHistory,
            TabAction.CloseDrawer,
            TabAction.DismissRename,
            TabAction.DismissReset,
            TabAction.DismissClearTab,
        )
        actions.forEach { assertFalse("$it should keep the keyboard", it.dismissesKeyboard) }
    }
}
