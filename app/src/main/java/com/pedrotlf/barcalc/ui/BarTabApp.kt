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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedrotlf.barcalc.R
import com.pedrotlf.barcalc.data.SessionRepository
import com.pedrotlf.barcalc.data.history.BarCalcDatabase
import com.pedrotlf.barcalc.data.history.RoomHistoryStore
import com.pedrotlf.barcalc.data.receipt.MlKitReceiptTextRecognizer
import com.pedrotlf.barcalc.ui.components.LocalCurrencySymbol
import com.pedrotlf.barcalc.ui.screens.AboutSheet
import com.pedrotlf.barcalc.ui.screens.AppDrawer
import com.pedrotlf.barcalc.ui.screens.ClaimSheet
import com.pedrotlf.barcalc.ui.screens.ConfirmDialog
import com.pedrotlf.barcalc.ui.screens.HistoryScreen
import com.pedrotlf.barcalc.ui.screens.ItemsScreen
import com.pedrotlf.barcalc.ui.screens.PeopleScreen
import com.pedrotlf.barcalc.ui.screens.RenameEntryDialog
import com.pedrotlf.barcalc.ui.screens.ResetConfirmDialog
import com.pedrotlf.barcalc.ui.screens.ResultsScreen
import com.pedrotlf.barcalc.ui.screens.ScanResultScreen
import com.pedrotlf.barcalc.ui.theme.BarTabColors

/** Root of the wizard: screen switching, claim sheet overlay, back handling. */
@Composable
fun BarTabApp(vm: TabViewModel? = null) {
    val appContext = LocalContext.current.applicationContext
    @Suppress("NAME_SHADOWING")
    val vm = vm ?: viewModel {
        TabViewModel(
            repository = SessionRepository(appContext),
            history = RoomHistoryStore(BarCalcDatabase.get(appContext).tabHistoryDao()),
            textRecognizer = MlKitReceiptTextRecognizer(appContext),
        )
    }
    val state by vm.uiState.collectAsState()

    // Opening a sheet or leaving the screen shouldn't leave the keyboard
    // covering what was just opened. Handled here, at the single entry point
    // every screen already routes through, rather than at each call site.
    // Focus is cleared too, otherwise the field it belongs to just summons the
    // keyboard back.
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val onAction: (TabAction) -> Unit = { action ->
        if (action.dismissesKeyboard) {
            focusManager.clearFocus()
            keyboard?.hide()
        }
        vm.onAction(action)
    }

    BackHandler(
        enabled = state.screen != Screen.ITEMS ||
            state.activePersonId != null ||
            state.showAbout ||
            state.showResetConfirm ||
            state.showDrawer ||
            state.showHistory ||
            state.pendingDuplicateId != null ||
            state.renamingEntryId != null ||
            state.pendingDeleteEntryId != null ||
            state.showClearHistoryConfirm ||
            state.scanResult != null,
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
                // Everything except the IME — the scaffold handles the keyboard itself.
                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout)),
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

            AnimatedVisibility(
                visible = state.showResetConfirm,
                enter = fadeIn(tween(160)) +
                    slideInVertically(tween(160)) { it / 40 } +
                    scaleIn(tween(160), initialScale = 0.97f),
                exit = fadeOut(tween(120)),
            ) {
                ResetConfirmDialog(onAction)
            }

            AnimatedVisibility(
                visible = state.scanning || state.scanResult != null,
                enter = fadeIn(tween(160)),
                exit = fadeOut(tween(120)),
            ) {
                ScanResultScreen(state.scanning, state.scanResult, onAction)
            }

            // History lives above the wizard but below the drawer and dialogs.
            AnimatedVisibility(
                visible = state.showHistory,
                enter = fadeIn(tween(160)),
                exit = fadeOut(tween(120)),
            ) {
                HistoryScreen(state.history, onAction)
            }

            AppDrawer(visible = state.showDrawer, onAction = onAction)

            AnimatedVisibility(
                visible = state.pendingDuplicateId != null,
                enter = fadeIn(tween(160)) + scaleIn(tween(160), initialScale = 0.97f),
                exit = fadeOut(tween(120)),
            ) {
                ConfirmDialog(
                    title = stringResource(R.string.history_replace_title),
                    message = stringResource(R.string.history_replace_message),
                    confirmText = stringResource(R.string.history_replace_confirm),
                    onConfirm = { onAction(TabAction.ConfirmDuplicate) },
                    onDismiss = { onAction(TabAction.DismissDuplicate) },
                )
            }

            AnimatedVisibility(
                visible = state.renamingEntryId != null,
                enter = fadeIn(tween(160)) + scaleIn(tween(160), initialScale = 0.97f),
                exit = fadeOut(tween(120)),
            ) {
                RenameEntryDialog(state.renameDraft, onAction)
            }

            AnimatedVisibility(
                visible = state.pendingDeleteEntryId != null,
                enter = fadeIn(tween(160)) + scaleIn(tween(160), initialScale = 0.97f),
                exit = fadeOut(tween(120)),
            ) {
                ConfirmDialog(
                    title = stringResource(R.string.history_delete_title),
                    message = stringResource(R.string.history_delete_message),
                    confirmText = stringResource(R.string.history_delete_confirm),
                    onConfirm = { onAction(TabAction.ConfirmDeleteEntry) },
                    onDismiss = { onAction(TabAction.DismissDeleteEntry) },
                )
            }

            AnimatedVisibility(
                visible = state.showClearHistoryConfirm,
                enter = fadeIn(tween(160)) + scaleIn(tween(160), initialScale = 0.97f),
                exit = fadeOut(tween(120)),
            ) {
                ConfirmDialog(
                    title = stringResource(R.string.history_clear_title),
                    message = stringResource(R.string.history_clear_message),
                    confirmText = stringResource(R.string.history_clear_confirm),
                    onConfirm = { onAction(TabAction.ConfirmClearHistory) },
                    onDismiss = { onAction(TabAction.DismissClearHistory) },
                )
            }
        }
    }
}
