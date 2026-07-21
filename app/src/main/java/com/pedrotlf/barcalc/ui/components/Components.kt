package com.pedrotlf.barcalc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrotlf.barcalc.R
import com.pedrotlf.barcalc.ui.theme.BarTabColors
import com.pedrotlf.barcalc.ui.theme.BarTabType
import com.pedrotlf.barcalc.ui.theme.Figtree

/**
 * The currency symbol to prefix money amounts with, provided once at the app
 * root from the localized `currency_symbol` string resource.
 */
val LocalCurrencySymbol = staticCompositionLocalOf { "$" }

private const val DISABLED_ALPHA = 0.45f

/** Accent-colored checkbox, shared by the tip toggle and the claim sheet. */
@Composable
fun accentCheckboxColors(): CheckboxColors = CheckboxDefaults.colors(
    checkedColor = BarTabColors.Accent500,
    checkmarkColor = BarTabColors.Bg,
    uncheckedColor = BarTabColors.Neutral400,
)

/** Pill primary button (.btn-primary): accent bg, bg-colored heading-font label. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clip(CircleShape)
            .background(BarTabColors.Accent500)
            .clickable(enabled = enabled, onClick = onClick)
            .defaultMinSize(minHeight = 46.dp)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = BarTabType.Button, color = BarTabColors.Bg)
    }
}

/** Square-ish pill icon button on accent bg (e.g. the 44dp "add" buttons). */
@Composable
fun PrimaryIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 44.dp,
    iconSize: Dp = 18.dp,
) {
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .size(size)
            .clip(CircleShape)
            .background(BarTabColors.Accent500)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, Modifier.size(iconSize), tint = BarTabColors.Bg)
    }
}

/** Ghost icon button (.btn-ghost .btn-icon): transparent, accent-tinted glyph. */
@Composable
fun GhostIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconSize: Dp = 15.dp,
    tint: Color = BarTabColors.Accent500,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, Modifier.size(iconSize), tint = tint)
    }
}

/** The three stepper sizes used across the app. */
enum class StepperSize(
    val button: Dp,
    val icon: Dp,
    val labelMinWidth: Dp,
    val labelSize: TextUnit,
    val gap: Dp,
) {
    /** Add-item card. */
    Regular(button = 36.dp, icon = 15.dp, labelMinWidth = 18.dp, labelSize = 15.sp, gap = 8.dp),

    /** Inside item rows. */
    Compact(button = 28.dp, icon = 13.dp, labelMinWidth = 14.dp, labelSize = 13.sp, gap = 2.dp),

    /** Tip percent control in the People footer. */
    Tip(button = 32.dp, icon = 14.dp, labelMinWidth = 36.dp, labelSize = 14.sp, gap = 8.dp),
}

/** Minus / label / plus stepper used for quantities and the tip percent. */
@Composable
fun QtyStepper(
    label: String,
    onDec: () -> Unit,
    onInc: () -> Unit,
    modifier: Modifier = Modifier,
    size: StepperSize = StepperSize.Regular,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(size.gap),
    ) {
        GhostIconButton(
            AppIcons.Minus,
            stringResource(R.string.cd_decrease),
            onDec,
            size = size.button,
            iconSize = size.icon,
        )
        Text(
            label,
            style = TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.SemiBold,
                fontSize = size.labelSize,
                color = BarTabColors.Text,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.widthIn(min = size.labelMinWidth),
        )
        GhostIconButton(
            AppIcons.Plus,
            stringResource(R.string.cd_increase),
            onInc,
            size = size.button,
            iconSize = size.icon,
        )
    }
}

/** Initials circle avatar. */
@Composable
fun Avatar(
    initials: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    fontSize: TextUnit = 13.sp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initials,
            style = TextStyle(
                fontFamily = Figtree,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                color = Color.White,
            ),
        )
    }
}

/**
 * Undecorated text field: just text + placeholder, for inline editing inside
 * rows and containers (the design's borderless `.input` usages).
 */
@Composable
fun BareTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = BarTabType.Label,
    placeholder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        cursorBrush = SolidColor(BarTabColors.Accent500),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty() && placeholder != null) {
                    Text(
                        placeholder,
                        style = textStyle.copy(color = BarTabColors.Neutral500),
                        maxLines = 1,
                    )
                }
                innerTextField()
            }
        },
    )
}

/** Pill text field (.input): surface bg, hairline divider border. */
@Composable
fun PillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .clip(CircleShape)
            .background(BarTabColors.Surface)
            .roundedBorder(BarTabColors.Divider, cornerRadius = 999.dp)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BareTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder,
            keyboardOptions = keyboardOptions,
        )
    }
}
