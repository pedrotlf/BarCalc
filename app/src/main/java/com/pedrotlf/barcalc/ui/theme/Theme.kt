package com.pedrotlf.barcalc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightScheme = lightColorScheme(
    primary = BarTabColors.Accent500,
    onPrimary = BarTabColors.Bg,
    secondary = BarTabColors.Accent2_500,
    onSecondary = BarTabColors.Bg,
    background = BarTabColors.Bg,
    onBackground = BarTabColors.Text,
    surface = BarTabColors.Surface,
    onSurface = BarTabColors.Text,
    outline = BarTabColors.Divider,
)

/** Single light theme — the design has one warm, light look. */
@Composable
fun BarCalcTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightScheme,
        content = content,
    )
}
