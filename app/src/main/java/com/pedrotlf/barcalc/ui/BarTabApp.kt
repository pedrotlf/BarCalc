package com.pedrotlf.barcalc.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrotlf.barcalc.R
import com.pedrotlf.barcalc.data.SessionRepository
import com.pedrotlf.barcalc.ui.components.LocalCurrencySymbol
import com.pedrotlf.barcalc.ui.screens.AboutSheet
import com.pedrotlf.barcalc.ui.screens.ClaimSheet
import com.pedrotlf.barcalc.ui.screens.ItemsScreen
import com.pedrotlf.barcalc.ui.screens.PeopleScreen
import com.pedrotlf.barcalc.ui.screens.ResultsScreen
import com.pedrotlf.barcalc.ui.theme.BarTabColors

/** Root of the wizard: screen switching, claim sheet overlay, back handling. */
@Composable
fun BarTabApp(vm: TabViewModel? = null) {
    val appContext = LocalContext.current.applicationContext
    @Suppress("NAME_SHADOWING")
    val vm = vm ?: viewModel { TabViewModel(SessionRepository(appContext)) }
    val state by vm.uiState.collectAsState()
    val onAction = vm::onAction

    BackHandler(
        enabled = state.screen != Screen.ITEMS || state.activePersonId != null || state.showAbout,
    ) {
        onAction(TabAction.Back)
    }

    CompositionLocalProvider(
        LocalCurrencySymbol provides stringResource(R.string.currency_symbol),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(BarTabColors.Bg)
                .safeDrawingPadding(),
        ) {
            Crossfade(targetState = state.screen, animationSpec = tween(180), label = "screen") { screen ->
                when (screen) {
                    Screen.ITEMS -> ItemsScreen(state, onAction)
                    Screen.PEOPLE -> PeopleScreen(state, onAction)
                    Screen.RESULTS -> ResultsScreen(state, onAction)
                }
            }

            val activePerson = state.activePerson
            AnimatedVisibility(
                visible = activePerson != null && state.screen == Screen.PEOPLE,
                enter = fadeIn(tween(160)) +
                    slideInVertically(tween(160)) { it / 40 } +
                    scaleIn(tween(160), initialScale = 0.97f),
                exit = fadeOut(tween(120)),
            ) {
                if (activePerson != null) {
                    ClaimSheet(activePerson, state.activePersonIndex, state, onAction)
                }
            }

            AnimatedVisibility(
                visible = state.showAbout,
                enter = fadeIn(tween(160)) +
                    slideInVertically(tween(160)) { it / 40 } +
                    scaleIn(tween(160), initialScale = 0.97f),
                exit = fadeOut(tween(120)),
            ) {
                AboutSheet(onAction)
            }
        }
    }
}
