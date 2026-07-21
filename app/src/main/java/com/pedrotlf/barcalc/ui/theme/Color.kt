package com.pedrotlf.barcalc.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Design tokens ported from the "Organic" design system (styles.css of the
 * claude.ai/design project). Tonal ramps share one lightness scale.
 */
object BarTabColors {
    val Bg = Color(0xFFF5EAD8)
    val Surface = Color(0xFFEBDDC5)
    val Text = Color(0xFF201E1D)
    val Divider = Text.copy(alpha = 0.16f)

    // Modal scrim: rgba(32,20,10,.45)
    val Scrim = Color(0x7320140A)

    val Neutral100 = Color(0xFFF9F4ED)
    val Neutral200 = Color(0xFFEEE7DB)
    val Neutral300 = Color(0xFFDCD3C4)
    val Neutral400 = Color(0xFFC0B6A5)
    val Neutral500 = Color(0xFFA19786)
    val Neutral600 = Color(0xFF82796A)
    val Neutral700 = Color(0xFF645C50)
    val Neutral800 = Color(0xFF474238)
    val Neutral900 = Color(0xFF2E2B25)

    val Accent100 = Color(0xFFFFF2EB)
    val Accent200 = Color(0xFFFFE1D0)
    val Accent300 = Color(0xFFFFC6A5)
    val Accent400 = Color(0xFFF6A06B)
    val Accent500 = Color(0xFFD67F48)
    val Accent600 = Color(0xFFB2622D)
    val Accent700 = Color(0xFF8C491A)
    val Accent800 = Color(0xFF643312)
    val Accent900 = Color(0xFF402310)

    val Accent2_100 = Color(0xFFF0FAE1)
    val Accent2_200 = Color(0xFFE1EECC)
    val Accent2_300 = Color(0xFFCCDBB2)
    val Accent2_400 = Color(0xFFAEBF92)
    val Accent2_500 = Color(0xFF8FA073)
    val Accent2_600 = Color(0xFF728157)
    val Accent2_700 = Color(0xFF56633F)
    val Accent2_800 = Color(0xFF3D472B)
    val Accent2_900 = Color(0xFF272E1B)

    /** Avatar palette: alternates accent / accent-2, like the design's colorForIndex. */
    fun avatarColor(index: Int): Color = if (index % 2 == 0) Accent500 else Accent2_500
}
