package com.pedrotlf.barcalc

import com.pedrotlf.barcalc.data.receipt.ReceiptTextRecognizer
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

/** Recognizer that answers with whatever the test asks for, no ML Kit involved. */
private class FakeRecognizer(private val answer: Result<String>) : ReceiptTextRecognizer {
    val seen = mutableListOf<String>()
    override suspend fun recognize(imageUri: String): Result<String> {
        seen += imageUri
        return answer
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReceiptScanTest {

    @Before
    fun setUpDispatcher() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDownDispatcher() = Dispatchers.resetMain()

    @Test
    fun `a captured photo is read and its text surfaced`() {
        val recognizer = FakeRecognizer(Result.success("2x Chopp  24,00"))
        val vm = TabViewModel(textRecognizer = recognizer)

        vm.onAction(TabAction.ReceiptCaptured("content://scan/1.jpg"))

        assertEquals(listOf("content://scan/1.jpg"), recognizer.seen)
        assertEquals(ScanResult.Text("2x Chopp  24,00"), vm.uiState.value.scanResult)
        assertFalse(vm.uiState.value.scanning)
    }

    @Test
    fun `a photo that can't be read reports a failure rather than empty text`() {
        val vm = TabViewModel(textRecognizer = FakeRecognizer(Result.failure(RuntimeException())))

        vm.onAction(TabAction.ReceiptCaptured("content://scan/broken.jpg"))

        assertEquals(ScanResult.Failed, vm.uiState.value.scanResult)
        assertFalse(vm.uiState.value.scanning)
    }

    @Test
    fun `a photo with no text is a success carrying nothing`() {
        // Distinct from a failure: the read worked, the tab just had nothing on
        // it, and the screen says so differently.
        val vm = TabViewModel(textRecognizer = FakeRecognizer(Result.success("")))

        vm.onAction(TabAction.ReceiptCaptured("content://scan/blank.jpg"))

        assertEquals(ScanResult.Text(""), vm.uiState.value.scanResult)
    }

    @Test
    fun `backing out of the scanner leaves no result behind`() {
        val vm = TabViewModel(textRecognizer = FakeRecognizer(Result.success("x")))

        vm.onAction(TabAction.ScanCancelled)

        assertNull(vm.uiState.value.scanResult)
        assertFalse(vm.uiState.value.scanning)
    }

    @Test
    fun `dismissing, and back, close the result`() {
        val recognizer = FakeRecognizer(Result.success("Chopp 12,00"))
        val vm = TabViewModel(textRecognizer = recognizer)

        vm.onAction(TabAction.ReceiptCaptured("content://scan/1.jpg"))
        vm.onAction(TabAction.DismissScanResult)
        assertNull(vm.uiState.value.scanResult)

        vm.onAction(TabAction.ReceiptCaptured("content://scan/1.jpg"))
        assertTrue(vm.goBack())
        assertNull(vm.uiState.value.scanResult)
    }

    @Test
    fun `scanning never touches the tab`() {
        // Phase one only reads text; nothing is added until the parser exists.
        val vm = TabViewModel(textRecognizer = FakeRecognizer(Result.success("2x Chopp 24,00")))
        vm.onNewItemNameChange("Beer")
        vm.onNewItemPriceChange(1000L)
        vm.addItem()

        vm.onAction(TabAction.ReceiptCaptured("content://scan/1.jpg"))

        assertEquals(1, vm.uiState.value.items.size)
        assertEquals("Beer", vm.uiState.value.items.single().name)
    }
}
