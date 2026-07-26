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
     * Touch target of the header's navigation slot. Always laid out, so the
     * title sits in exactly the same place on every screen and never jumps
     * when navigating. 48dp is Android's minimum accessible target.
     */
    val HeaderSlot = 48.dp

    /**
     * Glyph inside [HeaderSlot]. Larger than Material's stock 24dp because
     * this app's screen titles are 27sp rather than the ~20sp a standard app
     * bar assumes; shared by every nav control so the icon never changes size
     * between screens.
     */
    val HeaderIcon = 28.dp

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
