package com.pedrotlf.barcalc

import com.pedrotlf.barcalc.data.receipt.ChainedTabReader
import com.pedrotlf.barcalc.data.receipt.ReadingSource
import com.pedrotlf.barcalc.data.receipt.TabReader
import com.pedrotlf.barcalc.data.receipt.TabReading
import com.pedrotlf.barcalc.domain.receipt.ParsedItem
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class StubReader(
    private val answer: Result<TabReading>,
) : TabReader {
    var called = false
    override suspend fun read(imageUri: String): Result<TabReading> {
        called = true
        return answer
    }
}

private fun reading(vararg names: String, source: ReadingSource = ReadingSource.MODEL) =
    Result.success(
        TabReading(names.map { ParsedItem(it, 1000L, 1) }, "raw", source),
    )

/**
 * How the readers hand over. The model isn't on every device and doesn't always
 * find anything, so what matters is that text recognition still covers those
 * cases rather than the user getting nothing.
 */
class ChainedTabReaderTest {

    @Test
    fun `the first reader with items wins and the rest are left alone`() = runTest {
        val model = StubReader(reading("Chopp"))
        val text = StubReader(reading("Nachos", source = ReadingSource.TEXT))

        val result = ChainedTabReader(listOf(model, text)).read("uri").getOrThrow()

        assertEquals(listOf("Chopp"), result.items.map { it.name })
        assertEquals(ReadingSource.MODEL, result.source)
        assertFalse("text recognition shouldn't run once the model succeeded", text.called)
    }

    @Test
    fun `a reader that fails hands over to the next`() = runTest {
        val model = StubReader(Result.failure(RuntimeException("no Nano on this device")))
        val text = StubReader(reading("Nachos", source = ReadingSource.TEXT))

        val result = ChainedTabReader(listOf(model, text)).read("uri").getOrThrow()

        assertTrue(model.called)
        assertEquals(listOf("Nachos"), result.items.map { it.name })
        assertEquals(ReadingSource.TEXT, result.source)
    }

    @Test
    fun `a reader that finds nothing also hands over`() = runTest {
        // Succeeding with an empty list is no more use than failing, so the
        // chain keeps going rather than reporting an empty scan.
        val model = StubReader(reading())
        val text = StubReader(reading("Nachos", source = ReadingSource.TEXT))

        val result = ChainedTabReader(listOf(model, text)).read("uri").getOrThrow()

        assertEquals(listOf("Nachos"), result.items.map { it.name })
    }

    @Test
    fun `when every reader fails the failure is reported`() = runTest {
        val chain = ChainedTabReader(
            listOf(
                StubReader(Result.failure(RuntimeException("no Nano"))),
                StubReader(Result.failure(RuntimeException("unreadable image"))),
            ),
        )

        assertTrue(chain.read("uri").isFailure)
    }

    @Test
    fun `when readers merely find nothing that is an empty reading, not a failure`() = runTest {
        // The photo was read fine, it just had no items on it — the review
        // screen says something different for that than for a failure.
        val chain = ChainedTabReader(listOf(StubReader(reading()), StubReader(reading())))

        val result = chain.read("uri")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().items.isEmpty())
    }
}
