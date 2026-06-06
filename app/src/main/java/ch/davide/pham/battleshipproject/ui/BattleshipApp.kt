package ch.davide.pham.battleshipproject.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.davide.pham.battleshipproject.ui.components.SeaSceneBackground
import ch.davide.pham.battleshipproject.ui.components.SeaSceneMood
import ch.davide.pham.battleshipproject.ui.screens.AboutScreen
import ch.davide.pham.battleshipproject.ui.screens.BattleScreen
import ch.davide.pham.battleshipproject.ui.screens.ConnectingScreen
import ch.davide.pham.battleshipproject.ui.screens.FleetSetupScreen
import ch.davide.pham.battleshipproject.ui.screens.HomeScreen
import ch.davide.pham.battleshipproject.ui.screens.HowToPlayScreen
import ch.davide.pham.battleshipproject.ui.screens.JoinGameScreen
import ch.davide.pham.battleshipproject.ui.screens.ResultScreen
import ch.davide.pham.battleshipproject.ui.screens.SettingsScreen
import ch.davide.pham.battleshipproject.ui.theme.DeepNavy

/*
 * Author: Davide Pham
 * OpenAI Codex assisted with implementation; see README.md for attribution.
 */
@Composable
fun BattleshipApp(viewModel: BattleshipViewModel = viewModel()) {
    val state = viewModel.uiState
    val text = state.language.strings
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = state.stage != AppStage.HOME) {
        when (state.stage) {
            AppStage.JOIN,
            AppStage.SETTINGS,
            AppStage.HOW_TO_PLAY,
            AppStage.ABOUT -> viewModel.returnToHome()
            AppStage.FLEET_SETUP -> viewModel.openJoin(state.isHostMode)
            AppStage.CONNECTING -> viewModel.cancelConnection()
            AppStage.BATTLE -> showExitDialog = true
            AppStage.FINISHED -> viewModel.returnToHome()
            AppStage.HOME -> Unit
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Box(Modifier.fillMaxSize()) {
            SeaSceneBackground(
                mood = when {
                    state.stage == AppStage.FINISHED &&
                        state.outcome == GameOutcome.VICTORY -> SeaSceneMood.VICTORY
                    state.stage == AppStage.FINISHED -> SeaSceneMood.DEFEAT
                    else -> SeaSceneMood.DAY
                }
            )
            if (
                state.stage == AppStage.FLEET_SETUP ||
                state.stage == AppStage.CONNECTING ||
                state.stage == AppStage.BATTLE
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(DeepNavy.copy(alpha = 0.58f))
                )
            }
            when (state.stage) {
                AppStage.HOME -> HomeScreen(
                    state = state,
                    onEnter = { viewModel.openJoin(true) },
                    onSettings = viewModel::openSettings,
                    onHowToPlay = viewModel::openHowToPlay,
                    onAbout = viewModel::openAbout
                )

                AppStage.JOIN -> JoinGameScreen(
                    state = state,
                    onBack = viewModel::returnToHome,
                    onPlayerNameChange = viewModel::updatePlayerName,
                    onGameKeyChange = viewModel::updateGameKey,
                    onToggleMode = viewModel::openJoin,
                    onGenerateKey = viewModel::generateGameKey,
                    onPing = viewModel::checkServer,
                    onContinue = viewModel::openFleetSetup
                )

                AppStage.SETTINGS -> SettingsScreen(
                    state = state,
                    onBack = viewModel::returnToHome,
                    onLanguage = viewModel::setLanguage,
                    onToggleSound = viewModel::toggleSound,
                    onToggleMusic = viewModel::toggleMusic
                )

                AppStage.HOW_TO_PLAY -> HowToPlayScreen(
                    state = state,
                    onBack = viewModel::returnToHome
                )

                AppStage.ABOUT -> AboutScreen(
                    state = state,
                    onBack = viewModel::returnToHome
                )

                AppStage.FLEET_SETUP -> FleetSetupScreen(
                    state = state,
                    onBack = { viewModel.openJoin(state.isHostMode) },
                    onSelectShip = viewModel::selectShip,
                    onRotate = viewModel::rotateSelectedShip,
                    onRandomize = viewModel::randomizeFleet,
                    onPlace = viewModel::placeSelectedShip,
                    onDeploy = viewModel::deployFleet
                )

                AppStage.CONNECTING -> ConnectingScreen(
                    state = state,
                    onCancel = viewModel::cancelConnection,
                    onHome = viewModel::returnToHome
                )

                AppStage.BATTLE -> BattleScreen(
                    state = state,
                    onFire = viewModel::fireAt,
                    onRetry = viewModel::retryEnemyMove,
                    onPause = viewModel::togglePause,
                    onExit = { showExitDialog = true }
                )

                AppStage.FINISHED -> ResultScreen(
                    state = state,
                    onRematch = viewModel::prepareRematch,
                    onHome = viewModel::returnToHome
                )
            }
        }
    }

    state.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text(text.commandNotice) },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = viewModel::dismissError) {
                    Text(text.understood)
                }
            }
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(text.leaveTitle) },
            text = { Text(text.leaveMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        viewModel.returnToHome()
                    }
                ) {
                    Text(text.leaveBattle)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(text.stay)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
