package com.pedrotlf.barcalc.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Stroke icons matching the design's inline SVGs
 * (stroke-width 2.75, round caps/joins, 24x24 viewBox).
 * Drawn in black; tint them via [androidx.compose.material3.Icon]'s tint.
 */
object AppIcons {

    private fun strokeIcon(name: String, vararg paths: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            paths.forEach { d ->
                addPath(
                    pathData = addPathNodes(d),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2.75f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()

    val Plus: ImageVector by lazy {
        strokeIcon("Plus", "M12 5 L12 19", "M5 12 L19 12")
    }

    val Minus: ImageVector by lazy {
        strokeIcon("Minus", "M5 12 L19 12")
    }

    val Trash: ImageVector by lazy {
        strokeIcon(
            "Trash",
            "M3 6h18",
            "M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2",
            "M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6",
        )
    }

    /**
     * Sweeping away the whole tab, as opposed to [Trash] deleting one row.
     *
     * Kept to three paths at the set's 2.75 stroke: finer detail (bristles,
     * sweep marks, a hollow handle) needs a much lighter stroke and would sit
     * oddly beside [History] and [Menu] in the same header. The binding line
     * is load-bearing — without it the head reads as a shovel.
     *
     * Laid on a 45° diagonal so it spans 3..21 in both axes, like [History]
     * does; a steeper, more upright broom has a narrow bounding box and reads
     * as the smaller icon of the pair however much it is scaled up.
     */
    val Broom: ImageVector by lazy {
        strokeIcon(
            "Broom",
            // Handle, ending at the midpoint of the binding below.
            "M21.0 3.0 L13.0 11.0",
            // Bristle block: flared, with the sweeping edge bowed outward.
            "M10.9 8.8 L3.0 13.2 Q4.3 19.7 10.8 21.0 L15.2 13.1 Z",
            // The binding.
            "M6.7 11.1 L12.9 17.3",
        )
    }

    val ChevronRight: ImageVector by lazy {
        strokeIcon("ChevronRight", "M9 18 L15 12 L9 6")
    }

    val ArrowLeft: ImageVector by lazy {
        strokeIcon("ArrowLeft", "M19 12 L5 12", "M12 19 L5 12 L12 5")
    }

    val Close: ImageVector by lazy {
        strokeIcon("Close", "M18 6 L6 18", "M6 6 L18 18")
    }

    val Warning: ImageVector by lazy {
        strokeIcon(
            "Warning",
            "M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z",
            "M12 9 L12 13",
            "M12 17 L12.01 17",
        )
    }

    val Menu: ImageVector by lazy {
        strokeIcon("Menu", "M4 6h16", "M4 12h16", "M4 18h16")
    }

    val History: ImageVector by lazy {
        strokeIcon(
            "History",
            "M3.05 11a9 9 0 1 1 .5 4",
            "M3 21v-6h6",
            "M12 7v5l3.5 2",
        )
    }

    val Pencil: ImageVector by lazy {
        strokeIcon(
            "Pencil",
            "M12 20h9",
            "M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4z",
        )
    }

    val Help: ImageVector by lazy {
        strokeIcon(
            "Help",
            "M12 2 a10 10 0 1 0 0 20 a10 10 0 1 0 0 -20",
            "M9.09 9 a3 3 0 0 1 5.83 1 c0 2 -3 3 -3 3",
            "M12 17 L12.01 17",
        )
    }
}
