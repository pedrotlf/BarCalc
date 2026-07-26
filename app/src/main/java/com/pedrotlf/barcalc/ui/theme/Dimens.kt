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

    /**
     * Size of the reserved slots either side of a screen header. Both are
     * always laid out, empty or not, so the title sits in exactly the same
     * place on every screen and never jumps when navigating.
     */
    val HeaderSlot = 44.dp

    /**
     * Header rows inset less than [ScreenHPadding] because the slot's touch
     * target is wider than its glyph — this lands the icon roughly over the
     * content edge below it.
     */
    val HeaderHPadding = 8.dp

    val RadiusSm = 4.dp
    val RadiusMd = 8.dp

    /** Item rows, add-cards, the claim sheet. */
    val RadiusLg = 16.dp

    /** Person/result cards (design: radius-lg * 1.15). */
    val RadiusXl = 32.dp

    /**
     * Centered modal sheets (claim / about) take this share of the screen
     * width, so they scale with the device — near-full in phone portrait, and
     * proportionally wider in landscape instead of staying narrow.
     */
    const val SheetWidthFraction = 0.85f

    /** …but never exceed this, so text stays readable on large tablets. */
    val SheetMaxWidth = 720.dp
}
