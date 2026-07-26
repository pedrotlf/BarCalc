package com.pedrotlf.barcalc.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Flows its children onto as many rows as they need, like `FlowRow`, with one
 * difference: the **first** child is flexible and stretches to fill whatever
 * space is left on its row.
 *
 * Wrapping is decided by the children themselves — every child is measured at
 * the width it actually wants, and the flexible one contributes its *minimum*
 * intrinsic width (so give it a `widthIn(min = …)` to say how narrow it may
 * legibly get). Nothing here consults the screen size, so the layout keeps
 * working as text, currency symbols, translations and font scale change,
 * without a breakpoint to maintain.
 *
 * Children that must stay together on the same row should be wrapped in a
 * single composable, since each direct child wraps independently.
 */
@Composable
fun FlexibleFlowRow(
    modifier: Modifier = Modifier,
    horizontalGap: Dp = 0.dp,
    verticalGap: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        if (measurables.isEmpty()) return@Layout layout(0, 0) {}

        val hGap = horizontalGap.roundToPx()
        val vGap = verticalGap.roundToPx()
        val maxWidth = constraints.maxWidth

        // Everything after the first child keeps its natural width.
        val fixed = measurables.drop(1).map { it.measure(Constraints(maxWidth = maxWidth)) }

        // The flexible child only claims its minimum while we decide where the
        // rows break; it is re-measured to fill its row afterwards.
        val flexMin = measurables.first()
            .minIntrinsicWidth(constraints.maxHeight)
            .coerceIn(0, maxWidth)

        val widths = IntArray(measurables.size)
        widths[0] = flexMin
        fixed.forEachIndexed { i, placeable -> widths[i + 1] = placeable.width }

        val rows = packIntoRows(widths, maxWidth, hGap)
        val flexWidth = flexibleChildWidth(rows, widths, maxWidth, hGap)
        val flexPlaceable = measurables.first()
            .measure(Constraints(minWidth = flexWidth, maxWidth = flexWidth))

        val placeables = buildList {
            add(flexPlaceable)
            addAll(fixed)
        }

        val rowHeights = rows.map { indices -> indices.maxOf { placeables[it].height } }
        val height = rowHeights.sum() + (rows.size - 1).coerceAtLeast(0) * vGap

        layout(maxWidth, height) {
            var y = 0
            rows.forEachIndexed { rowIndex, indices ->
                var x = 0
                indices.forEach { index ->
                    val placeable = placeables[index]
                    // Centre each child within its row, so differing heights
                    // (a 28dp button against a 44dp field) still line up.
                    placeable.placeRelative(x, y + (rowHeights[rowIndex] - placeable.height) / 2)
                    x += placeable.width + hGap
                }
                y += rowHeights[rowIndex] + vGap
            }
        }
    }
}

/**
 * Greedily groups child indices into rows: keep adding to the current row
 * until one doesn't fit, then start another. Pure so the wrapping rules can be
 * unit-tested without a device.
 */
internal fun packIntoRows(widths: IntArray, maxWidth: Int, gap: Int): List<List<Int>> {
    if (widths.isEmpty()) return emptyList()
    val rows = mutableListOf<MutableList<Int>>()
    var row = mutableListOf<Int>()
    var used = 0
    widths.forEachIndexed { index, width ->
        val needed = if (row.isEmpty()) width else used + gap + width
        if (row.isNotEmpty() && needed > maxWidth) {
            rows += row
            row = mutableListOf(index)
            used = width
        } else {
            row += index
            used = needed
        }
    }
    if (row.isNotEmpty()) rows += row
    return rows
}

/**
 * Width for the flexible child (index 0): everything left on its own row once
 * that row's other children have taken theirs, never below its minimum.
 */
internal fun flexibleChildWidth(
    rows: List<List<Int>>,
    widths: IntArray,
    maxWidth: Int,
    gap: Int,
): Int {
    val flexRow = rows.first { 0 in it }
    val taken = flexRow.filter { it != 0 }.sumOf { widths[it] } + (flexRow.size - 1) * gap
    return (maxWidth - taken).coerceAtLeast(widths[0])
}
