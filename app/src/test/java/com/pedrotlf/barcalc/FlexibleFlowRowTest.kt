package com.pedrotlf.barcalc

import com.pedrotlf.barcalc.ui.components.flexibleChildWidth
import com.pedrotlf.barcalc.ui.components.packIntoRows
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The wrapping rules behind [com.pedrotlf.barcalc.ui.components.FlexibleFlowRow].
 * Widths stand in for measured pixels; index 0 is always the flexible child,
 * contributing its *minimum* width while rows are decided.
 */
class FlexibleFlowRowTest {

    @Test
    fun `everything stays on one row when it fits`() {
        // flex(min 150) + controls(150), gap 10 -> 310 of 400.
        val widths = intArrayOf(150, 150)
        val rows = packIntoRows(widths, maxWidth = 400, gap = 10)
        assertEquals(listOf(listOf(0, 1)), rows)
    }

    @Test
    fun `the flexible child absorbs the leftover space on its row`() {
        val widths = intArrayOf(150, 150)
        val rows = packIntoRows(widths, maxWidth = 400, gap = 10)
        // 400 - 150 (controls) - 10 (gap) = 240 for the flexible child.
        assertEquals(240, flexibleChildWidth(rows, widths, maxWidth = 400, gap = 10))
    }

    @Test
    fun `a child that no longer fits moves to the next row`() {
        // 150 + 10 + 150 = 310 doesn't fit in 280.
        val widths = intArrayOf(150, 150)
        val rows = packIntoRows(widths, maxWidth = 280, gap = 10)
        assertEquals(listOf(listOf(0), listOf(1)), rows)
    }

    @Test
    fun `a wrapped flexible child takes the full width`() {
        val widths = intArrayOf(150, 150)
        val rows = packIntoRows(widths, maxWidth = 280, gap = 10)
        // Alone on its row, so it gets everything.
        assertEquals(280, flexibleChildWidth(rows, widths, maxWidth = 280, gap = 10))
    }

    @Test
    fun `an item row keeps name and price together and drops the controls`() {
        // name(min 96) + price(70) + controls(120), gap 6.
        val widths = intArrayOf(96, 70, 120)
        // Wide: 96 + 6 + 70 + 6 + 120 = 298 fits in 320.
        assertEquals(listOf(listOf(0, 1, 2)), packIntoRows(widths, maxWidth = 320, gap = 6))
        // Narrow: controls no longer fit, so they wrap on their own.
        val narrow = packIntoRows(widths, maxWidth = 260, gap = 6)
        assertEquals(listOf(listOf(0, 1), listOf(2)), narrow)
        // The name then stretches into what the controls left behind.
        assertEquals(184, flexibleChildWidth(narrow, widths, maxWidth = 260, gap = 6))
    }

    @Test
    fun `the flexible child never goes below its minimum`() {
        // Nothing fits: the flexible child still reports its floor rather than
        // a negative or clipped width.
        val widths = intArrayOf(150, 200)
        val rows = packIntoRows(widths, maxWidth = 120, gap = 10)
        assertEquals(listOf(listOf(0), listOf(1)), rows)
        assertEquals(150, flexibleChildWidth(rows, widths, maxWidth = 120, gap = 10))
    }

    @Test
    fun `rows keep filling while there is room`() {
        val widths = intArrayOf(50, 50, 50, 50)
        // 50+10+50+10+50 = 170 fits in 180; the fourth starts a new row.
        assertEquals(
            listOf(listOf(0, 1, 2), listOf(3)),
            packIntoRows(widths, maxWidth = 180, gap = 10),
        )
    }

    @Test
    fun `no children means no rows`() {
        assertEquals(emptyList<List<Int>>(), packIntoRows(intArrayOf(), maxWidth = 100, gap = 10))
    }
}
