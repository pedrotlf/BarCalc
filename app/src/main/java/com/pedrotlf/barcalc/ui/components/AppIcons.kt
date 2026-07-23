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

    val ChevronRight: ImageVector by lazy {
        strokeIcon("ChevronRight", "M9 18 L15 12 L9 6")
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

    val Help: ImageVector by lazy {
        strokeIcon(
            "Help",
            "M12 2 a10 10 0 1 0 0 20 a10 10 0 1 0 0 -20",
            "M9.09 9 a3 3 0 0 1 5.83 1 c0 2 -3 3 -3 3",
            "M12 17 L12.01 17",
        )
    }
}
