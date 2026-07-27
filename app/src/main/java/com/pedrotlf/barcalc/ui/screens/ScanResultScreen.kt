package com.pedrotlf.barcalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrotlf.barcalc.R
import com.pedrotlf.barcalc.ui.ScanResult
import com.pedrotlf.barcalc.ui.TabAction
import com.pedrotlf.barcalc.ui.components.PrimaryButton
import com.pedrotlf.barcalc.ui.theme.BarCalcTheme
import com.pedrotlf.barcalc.ui.theme.BarTabColors
import com.pedrotlf.barcalc.ui.theme.BarTabDimens
import com.pedrotlf.barcalc.ui.theme.BarTabType

/**
 * What the camera read off the tab.
 *
 * This is as far as scanning goes for now: the text is shown as it came back,
 * so the recognition can be judged on real tabs before anything tries to turn
 * it into items.
 */
@Composable
fun ScanResultScreen(scanning: Boolean, result: ScanResult?, onAction: (TabAction) -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(BarTabColors.Bg),
    ) {
        ScreenScaffold(
            footer = {
                PrimaryButton(
                    text = stringResource(R.string.close),
                    onClick = { onAction(TabAction.DismissScanResult) },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        ) {
            ScreenHeader(
                title = stringResource(R.string.scan_result_title),
                subtitle = stringResource(R.string.scan_result_subtitle),
                leading = { HeaderCloseButton { onAction(TabAction.DismissScanResult) } },
            )

            when {
                scanning -> ScanningIndicator()
                result is ScanResult.Failed -> EmptyListHint(stringResource(R.string.scan_failed))
                result is ScanResult.Text && result.text.isBlank() ->
                    EmptyListHint(stringResource(R.string.scan_empty))
                result is ScanResult.Text -> RecognizedText(result.text)
                else -> Unit
            }
        }
    }
}

@Composable
private fun ScanningIndicator() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = BarTabDimens.ScreenHPadding, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CircularProgressIndicator(
            color = BarTabColors.Accent500,
            modifier = Modifier.size(32.dp),
        )
        Text(
            stringResource(R.string.scan_reading),
            style = BarTabType.Hint,
            textAlign = TextAlign.Center,
        )
    }
}

/** The raw recognised text, in a monospaced-ish block so columns stay legible. */
@Composable
private fun RecognizedText(text: String) {
    Text(
        text,
        style = BarTabType.Body.copy(
            fontSize = 13.sp,
            color = BarTabColors.Neutral800,
            lineHeight = 19.sp,
        ),
        modifier = Modifier
            .padding(
                start = BarTabDimens.ScreenHPadding,
                top = 12.dp,
                end = BarTabDimens.ScreenHPadding,
                bottom = 16.dp,
            )
            .fillMaxWidth()
            .clip(RoundedCornerShape(BarTabDimens.RadiusLg))
            .background(BarTabColors.Surface)
            .padding(BarTabDimens.CardPadding),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF5EAD8, heightDp = 700)
@Composable
private fun ScanResultScreenPreview() {
    BarCalcTheme {
        ScanResultScreen(
            scanning = false,
            result = ScanResult.Text(
                """
                BAR DO ZE
                2x Chopp        24,00
                Porcao Fritas   32,50
                Agua             6,00
                SUBTOTAL        62,50
                """.trimIndent(),
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Reading", showBackground = true, backgroundColor = 0xFFF5EAD8, heightDp = 400)
@Composable
private fun ScanResultScreenLoadingPreview() {
    BarCalcTheme {
        ScanResultScreen(scanning = true, result = null, onAction = {})
    }
}
