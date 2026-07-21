package com.pedrotlf.barcalc.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing and shape scale, mirroring the design system's --space-* and
 * --radius-* tokens (with the "rounded frame" overrides the design applies:
 * cards go radius-lg * 1.15, small controls go pill).
 */
object BarTabDimens {
    /** Horizontal padding of every screen section. */
    val ScreenHPadding = 20.dp

    /** Gap between stacked list rows / footer children. */
    val ListGap = 10.dp

    /** Inner padding of the dashed add-cards. */
    val CardPadding = 14.dp

    val RadiusSm = 4.dp
    val RadiusMd = 8.dp

    /** Item rows, add-cards, the claim sheet. */
    val RadiusLg = 16.dp

    /** Person/result cards (design: radius-lg * 1.15). */
    val RadiusXl = 32.dp
}
