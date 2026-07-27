package com.pedrotlf.barcalc

import com.pedrotlf.barcalc.data.receipt.ModelAvailability
import com.pedrotlf.barcalc.data.receipt.ModelAvailabilityProbe
import com.pedrotlf.barcalc.data.receipt.ModelStatus
import com.pedrotlf.barcalc.data.receipt.ReadingSource
import com.pedrotlf.barcalc.data.receipt.TabReader
import com.pedrotlf.barcalc.data.receipt.TabReading
import com.pedrotlf.barcalc.domain.receipt.ReceiptParser
import com.pedrotlf.barcalc.ui.ScanResult
import com.pedrotlf.barcalc.ui.TabAction
import com.pedrotlf.barcalc.ui.TabViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

/** Reader that answers with whatever the test asks for, no ML Kit involved. */
private class FakeRecognizer(private val answer: Result<String>) : TabReader {
    val seen = mutableListOf<String>()
    override suspend fun read(imageUri: String): Result<TabReading> {
        seen += imageUri
        return answer.map { TabReading(ReceiptParser.parse(it), it, ReadingSource.TEXT) }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReceiptScanTest {

    @Before
    fun setUpDispatcher() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDownDispatcher() = Dispatchers.resetMain()

    @Test
    fun `the model's availability is refreshed with each scan`() {
        // Which reader won doesn't say whether the model was missing, still
        // downloading, or just found nothing — this is what distinguishes them.
        val probe = object : ModelAvailabilityProbe {
            override suspend fun status() = ModelStatus(ModelAvailability.UNSUPPORTED, "stub")
        }
        val vm = TabViewModel(
            tabReader = FakeRecognizer(Result.success("Nachos 12,00")),
            modelProbe = probe,
        )
        assertEquals(ModelAvailability.UNKNOWN, vm.uiState.value.modelAvailability)

        vm.onAction(TabAction.ReceiptCaptured("content://scan/1.jpg"))

        assertEquals(ModelAvailability.UNSUPPORTED, vm.uiState.value.modelAvailability)
    }

    @Test
    fun `a captured photo is read and its text surfaced`() {
        val recognizer = FakeRecognizer(Result.success("2x Chopp  24,00"))
        val vm = TabViewModel(tabReader = recognizer)

        vm.onAction(TabAction.ReceiptCaptured("content://scan/1.jpg"))

        assertEquals(listOf("content://scan/1.jpg"), recognizer.seen)
        val read = vm.uiState.value.scanResult as ScanResult.Read
        assertEquals("2x Chopp  24,00", read.rawText)
        val draft = read.drafts.single()
        assertEquals("Chopp", draft.name)
        assertEquals(1200L, draft.priceCents)
        assertEquals(2, draft.qty)
        assertTrue("scanned lines start out kept", draft.included)
        assertFalse(vm.uiState.value.scanning)
    }

    @Test
    fun `a photo that can't be read reports a failure rather than empty text`() {
        val vm = TabViewModel(tabReader = FakeRecognizer(Result.failure(RuntimeException())))

        vm.onAction(TabAction.ReceiptCaptured("content://scan/broken.jpg"))

        assertEquals(ScanResult.Failed, vm.uiState.value.scanResult)
        assertFalse(vm.uiState.value.scanning)
    }

    @Test
    fun `a photo with no text is a success carrying nothing`() {
        // Distinct from a failure: the read worked, the tab just had nothing on
        // it, and the screen says so differently.
        val vm = TabViewModel(tabReader = FakeRecognizer(Result.success("")))

        vm.onAction(TabAction.ReceiptCaptured("content://scan/blank.jpg"))

        val read = vm.uiState.value.scanResult as ScanResult.Read
        assertEquals("", read.rawText)
        assertTrue(read.drafts.isEmpty())
    }

    @Test
    fun `backing out of the scanner leaves no result behind`() {
        val vm = TabViewModel(tabReader = FakeRecognizer(Result.success("x")))

        vm.onAction(TabAction.ScanCancelled)

        assertNull(vm.uiState.value.scanResult)
        assertFalse(vm.uiState.value.scanning)
    }

    @Test
    fun `dismissing, and back, close the result`() {
        val recognizer = FakeRecognizer(Result.success("Chopp 12,00"))
        val vm = TabViewModel(tabReader = recognizer)

        vm.onAction(TabAction.ReceiptCaptured("content://scan/1.jpg"))
        vm.onAction(TabAction.DismissScanResult)
        assertNull(vm.uiState.value.scanResult)

        vm.onAction(TabAction.ReceiptCaptured("content://scan/1.jpg"))
        assertTrue(vm.goBack())
        assertNull(vm.uiState.value.scanResult)
    }

    // ── Checking over a scan ───────────────────────────────────────────────

    private fun scannedVm(text: String): TabViewModel {
        val vm = TabViewModel(tabReader = FakeRecognizer(Result.success(text)))
        vm.onAction(TabAction.ReceiptCaptured("content://scan/1.jpg"))
        return vm
    }

    private val TabViewModel.read get() = uiState.value.scanResult as ScanResult.Read

    @Test
    fun `confirming adds the kept lines to the tab`() {
        val vm = scannedVm("2x Chopp 24,00\nNachos 12,00")

        vm.onAction(TabAction.ConfirmScannedItems)

        val items = vm.uiState.value.items
        assertEquals(listOf("Chopp", "Nachos"), items.map { it.name })
        assertEquals(listOf(1200L, 1200L), items.map { it.priceCents })
        assertEquals(listOf(2, 1), items.map { it.qty })
        assertNull(vm.uiState.value.scanResult)
    }

    @Test
    fun `unticked lines are left out`() {
        val vm = scannedVm("2x Chopp 24,00\nNachos 12,00")
        val nachos = vm.read.drafts.single { it.name == "Nachos" }

        vm.onAction(TabAction.ToggleScanDraft(nachos.id))
        vm.onAction(TabAction.ConfirmScannedItems)

        assertEquals(listOf("Chopp"), vm.uiState.value.items.map { it.name })
    }

    @Test
    fun `corrections made in the review are what get added`() {
        val vm = scannedVm("Nachos 12,00")
        val draft = vm.read.drafts.single()

        vm.onAction(TabAction.ScanDraftNameChanged(draft.id, "Nachos grandes"))
        vm.onAction(TabAction.ScanDraftPriceChanged(draft.id, 1550L))
        vm.onAction(TabAction.IncScanDraftQty(draft.id))
        vm.onAction(TabAction.ConfirmScannedItems)

        val item = vm.uiState.value.items.single()
        assertEquals("Nachos grandes", item.name)
        assertEquals(1550L, item.priceCents)
        assertEquals(2, item.qty)
    }

    @Test
    fun `scanned items are added to a tab already in progress, not replacing it`() {
        val vm = TabViewModel(tabReader = FakeRecognizer(Result.success("Nachos 12,00")))
        vm.onNewItemNameChange("Beer")
        vm.onNewItemPriceChange(1000L)
        vm.addItem()

        vm.onAction(TabAction.ReceiptCaptured("content://scan/1.jpg"))
        vm.onAction(TabAction.ConfirmScannedItems)

        assertEquals(listOf("Beer", "Nachos"), vm.uiState.value.items.map { it.name })
        // Ids have to keep climbing, or the new rows would collide with the old.
        assertEquals(2, vm.uiState.value.items.map { it.id }.distinct().size)
    }

    @Test
    fun `a line emptied in the review is not added`() {
        val vm = scannedVm("Nachos 12,00\n2x Chopp 24,00")
        val nachos = vm.read.drafts.single { it.name == "Nachos" }

        vm.onAction(TabAction.ScanDraftNameChanged(nachos.id, "   "))
        vm.onAction(TabAction.ConfirmScannedItems)

        assertEquals(listOf("Chopp"), vm.uiState.value.items.map { it.name })
    }

    @Test
    fun `the running total counts only the kept lines`() {
        val vm = scannedVm("2x Chopp 24,00\nNachos 12,00")
        assertEquals(3600L, vm.read.includedTotalCents)

        vm.onAction(TabAction.ToggleScanDraft(vm.read.drafts.single { it.name == "Nachos" }.id))
        assertEquals(2400L, vm.read.includedTotalCents)
    }

    @Test
    fun `closing the review throws the scan away`() {
        val vm = scannedVm("Nachos 12,00")

        vm.onAction(TabAction.DismissScanResult)

        assertNull(vm.uiState.value.scanResult)
        assertTrue(vm.uiState.value.items.isEmpty())
    }

    @Test
    fun `quantity in the review stays at one or above`() {
        val vm = scannedVm("Nachos 12,00")
        val draft = vm.read.drafts.single()

        repeat(3) { vm.onAction(TabAction.DecScanDraftQty(draft.id)) }

        assertEquals(1, vm.read.drafts.single().qty)
    }

    @Test
    fun `scanning alone never touches the tab`() {
        // Phase one only reads text; nothing is added until the parser exists.
        val vm = TabViewModel(tabReader = FakeRecognizer(Result.success("2x Chopp 24,00")))
        vm.onNewItemNameChange("Beer")
        vm.onNewItemPriceChange(1000L)
        vm.addItem()

        vm.onAction(TabAction.ReceiptCaptured("content://scan/1.jpg"))

        assertEquals(1, vm.uiState.value.items.size)
        assertEquals("Beer", vm.uiState.value.items.single().name)
    }
}
