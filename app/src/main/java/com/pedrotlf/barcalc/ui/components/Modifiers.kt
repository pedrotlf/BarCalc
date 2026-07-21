package com.pedrotlf.barcalc.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 1dp solid rounded border drawn behind content. */
fun Modifier.roundedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 1.dp,
): Modifier = drawBehind {
    val radius = cornerRadius.toPx().coerceAtMost(size.minDimension / 2)
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(radius),
        style = Stroke(width = strokeWidth.toPx()),
    )
}

/** Dashed rounded border, like the add-item / add-person drop zones. */
fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 1.5.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 5.dp,
): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashLength.toPx(), gapLength.toPx())
            ),
        ),
    )
}

/** 1dp hairline along the top edge. */
fun Modifier.topBorder(color: Color): Modifier = drawBehind {
    drawLine(
        color = color,
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
        strokeWidth = 1.dp.toPx(),
    )
}

/** 1dp hairline along the bottom edge. */
fun Modifier.bottomBorder(color: Color): Modifier = drawBehind {
    drawLine(
        color = color,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = 1.dp.toPx(),
    )
}
