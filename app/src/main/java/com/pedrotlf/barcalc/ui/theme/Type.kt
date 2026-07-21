package com.pedrotlf.barcalc.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pedrotlf.barcalc.R

/** Heading font — Caprasimo, regular only (matches --font-heading). */
val Caprasimo = FontFamily(
    Font(R.font.caprasimo_regular, FontWeight.Normal)
)

/** Body font — Figtree variable font at 400/600/700 (matches --font-body). */
@OptIn(ExperimentalTextApi::class)
val Figtree = FontFamily(
    Font(
        R.font.figtree_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.figtree_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        R.font.figtree_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
)

object BarTabType {
    /** 27px Caprasimo screen titles ("Bar Tab", "Who's splitting?", …). */
    val ScreenTitle = TextStyle(
        fontFamily = Caprasimo,
        fontWeight = FontWeight.Normal,
        fontSize = 27.sp,
        color = BarTabColors.Text,
    )

    /** 13px subtitle under screen titles. */
    val ScreenSubtitle = TextStyle(
        fontFamily = Figtree,
        fontSize = 13.sp,
        color = BarTabColors.Neutral700,
    )

    /** Default body text. */
    val Body = TextStyle(
        fontFamily = Figtree,
        fontSize = 15.sp,
        color = BarTabColors.Text,
    )

    /** Buttons use the heading face at 14px, like .btn. */
    val Button = TextStyle(
        fontFamily = Caprasimo,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    )

    // ── Recurring semantic styles (one-off tweaks stay inline via .copy) ──

    /** 14px body text (footer labels, checkboxes). */
    val Label = Body.copy(fontSize = 14.sp)

    /** 14px muted (e.g. the "Subtotal" caption). */
    val LabelMuted = Label.copy(color = BarTabColors.Neutral700)

    /** 12px muted captions (prices in rows, claim summaries). */
    val Caption = Body.copy(fontSize = 12.sp, color = BarTabColors.Neutral600)

    /** Row titles: 14px semibold (item/claim row names). */
    val RowTitle = Body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

    /** Money amounts in rows: 13px bold. */
    val Money = Body.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold)

    /** Small accent hints ("Add at least one item…"). */
    val Hint = Body.copy(fontSize = 12.sp, color = BarTabColors.Accent700)

    /** Empty-state hints under the add-cards. */
    val EmptyHint = Body.copy(fontSize = 13.5.sp, color = BarTabColors.Neutral600)
}
