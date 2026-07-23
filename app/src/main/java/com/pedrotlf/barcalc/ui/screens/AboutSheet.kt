package com.pedrotlf.barcalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrotlf.barcalc.BuildConfig
import com.pedrotlf.barcalc.R
import com.pedrotlf.barcalc.ui.TabAction
import com.pedrotlf.barcalc.ui.components.AppIcons
import com.pedrotlf.barcalc.ui.components.GhostIconButton
import com.pedrotlf.barcalc.ui.components.PrimaryButton
import com.pedrotlf.barcalc.ui.theme.BarCalcTheme
import com.pedrotlf.barcalc.ui.theme.BarTabColors
import com.pedrotlf.barcalc.ui.theme.BarTabDimens
import com.pedrotlf.barcalc.ui.theme.BarTabType

private const val GITHUB_URL = "https://github.com/pedrotlf/BarCalc"
private const val GITHUB_LABEL = "github.com/pedrotlf/BarCalc"
private const val ISSUES_URL = "https://github.com/pedrotlf/BarCalc/issues"

/** Centered "About" modal: what the app is, who made it, and attribution. */
@Composable
fun AboutSheet(onAction: (TabAction) -> Unit) {
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(BarTabColors.Scrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onAction(TabAction.HideAbout) }
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        val sheetMaxHeight = maxHeight * 0.9f
        Column(
            Modifier
                .widthIn(max = 340.dp)
                .fillMaxWidth()
                .heightIn(max = sheetMaxHeight)
                .shadow(24.dp, RoundedCornerShape(BarTabDimens.RadiusLg))
                .clip(RoundedCornerShape(BarTabDimens.RadiusLg))
                .background(BarTabColors.Bg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { /* swallow clicks so the scrim doesn't close */ },
        ) {
            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.about_title),
                    style = BarTabType.ScreenTitle.copy(fontSize = 20.sp),
                    modifier = Modifier.weight(1f),
                )
                GhostIconButton(
                    icon = AppIcons.Close,
                    contentDescription = stringResource(R.string.cd_close),
                    onClick = { onAction(TabAction.HideAbout) },
                    size = 36.dp,
                    iconSize = 16.dp,
                )
            }

            // Body — sections separated by dividers, version pinned last.
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.app_name),
                        style = BarTabType.RowTitle.copy(fontSize = 16.sp),
                    )
                    Text(
                        stringResource(R.string.about_description),
                        style = BarTabType.Body.copy(
                            fontSize = 13.sp,
                            color = BarTabColors.Neutral700,
                            lineHeight = 19.sp,
                        ),
                    )
                    Text(
                        stringResource(R.string.about_privacy),
                        style = BarTabType.Body.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = BarTabColors.Accent2_600,
                            lineHeight = 19.sp,
                        ),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }

                SectionDivider()

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.about_developer),
                        style = BarTabType.Caption.copy(
                            color = BarTabColors.Neutral700,
                            lineHeight = 17.sp,
                        ),
                    )
                    Text(
                        stringResource(R.string.about_open_source),
                        style = BarTabType.Caption.copy(color = BarTabColors.Neutral700),
                    )
                    val uriHandler = LocalUriHandler.current
                    Text(
                        GITHUB_LABEL,
                        style = BarTabType.Caption.copy(
                            color = BarTabColors.Accent700,
                            textDecoration = TextDecoration.Underline,
                        ),
                        modifier = Modifier.clickable { uriHandler.openUri(GITHUB_URL) },
                    )
                    Text(
                        stringResource(R.string.about_report_issue),
                        style = BarTabType.Caption.copy(
                            color = BarTabColors.Accent700,
                            textDecoration = TextDecoration.Underline,
                        ),
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clickable { uriHandler.openUri(ISSUES_URL) },
                    )
                }

                SectionDivider()

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(R.string.about_licenses_heading),
                        style = BarTabType.Body.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BarTabColors.Text,
                        ),
                    )
                    Text(
                        stringResource(R.string.about_licenses_body),
                        style = BarTabType.Caption.copy(lineHeight = 17.sp),
                    )
                }

                SectionDivider()

                Text(
                    stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                    style = BarTabType.Caption,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Footer
            Box(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)) {
                PrimaryButton(
                    text = stringResource(R.string.close),
                    onClick = { onAction(TabAction.HideAbout) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(color = BarTabColors.Surface, thickness = 1.dp)
}

@Preview(showBackground = true, backgroundColor = 0xFFF5EAD8, heightDp = 720)
@Composable
private fun AboutSheetPreview() {
    BarCalcTheme {
        AboutSheet(onAction = {})
    }
}
