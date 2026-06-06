package ch.davide.pham.battleshipproject.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ch.davide.pham.battleshipproject.ui.GameOutcome
import ch.davide.pham.battleshipproject.ui.GameUiState
import ch.davide.pham.battleshipproject.ui.ServerStatus
import ch.davide.pham.battleshipproject.ui.TurnState
import ch.davide.pham.battleshipproject.ui.components.SeaSceneBackground
import ch.davide.pham.battleshipproject.ui.components.SeaSceneMood
import ch.davide.pham.battleshipproject.ui.theme.MobileAndroidApplicationBattleshipProjectTheme
import ch.davide.pham.battleshipproject.model.GridCoordinate
import ch.davide.pham.battleshipproject.model.ShipType
import ch.davide.pham.battleshipproject.model.ShotResult

@Preview(
    name = "Home",
    showBackground = true,
    widthDp = 412,
    heightDp = 915
)
@Composable
private fun HomeScreenPreview() {
    MobileAndroidApplicationBattleshipProjectTheme(darkTheme = true) {
        Box {
            SeaSceneBackground()
            HomeScreen(
                state = GameUiState(serverStatus = ServerStatus.ONLINE),
                onEnter = {},
                onSettings = {},
                onHowToPlay = {},
                onAbout = {}
            )
        }
    }
}

@Preview(
    name = "Join Game",
    showBackground = true,
    widthDp = 412,
    heightDp = 915
)
@Composable
private fun JoinGameScreenPreview() {
    MobileAndroidApplicationBattleshipProjectTheme(darkTheme = true) {
        Box {
            SeaSceneBackground()
            JoinGameScreen(
                state = GameUiState(
                    playerName = "Davide",
                    gameKey = "fleet47",
                    serverStatus = ServerStatus.ONLINE
                ),
                onBack = {},
                onPlayerNameChange = {},
                onGameKeyChange = {},
                onToggleMode = {},
                onGenerateKey = {},
                onPing = {},
                onContinue = {}
            )
        }
    }
}

@Preview(
    name = "Fleet Deployment",
    showBackground = true,
    widthDp = 412,
    heightDp = 915
)
@Composable
private fun FleetSetupPreview() {
    MobileAndroidApplicationBattleshipProjectTheme(darkTheme = true) {
        Box {
            SeaSceneBackground()
            FleetSetupScreen(
                state = GameUiState(
                    playerName = "Davide",
                    gameKey = "fleet47",
                    serverStatus = ServerStatus.ONLINE
                ),
                onBack = {},
                onSelectShip = {},
                onRotate = {},
                onRandomize = {},
                onPlace = {},
                onDeploy = {}
            )
        }
    }
}

@Preview(
    name = "Live Battle",
    showBackground = true,
    widthDp = 412,
    heightDp = 915
)
@Composable
private fun BattleScreenPreview() {
    MobileAndroidApplicationBattleshipProjectTheme(darkTheme = true) {
        Box {
            SeaSceneBackground()
            BattleScreen(
                state = GameUiState(
                    playerName = "Davide",
                    gameKey = "fleet47",
                    serverStatus = ServerStatus.ONLINE,
                    turnState = TurnState.YOUR_TURN,
                    statusTitle = "Your turn",
                    statusMessage = "Select an unmarked sector and launch a salvo.",
                    outgoingShots = mapOf(
                        GridCoordinate(2, 3) to ShotResult.HIT,
                        GridCoordinate(6, 4) to ShotResult.MISS,
                        GridCoordinate(7, 7) to ShotResult.HIT
                    ),
                    incomingShots = mapOf(
                        GridCoordinate(1, 1) to ShotResult.MISS,
                        GridCoordinate(4, 5) to ShotResult.HIT
                    ),
                    enemyShipsSunk = setOf(ShipType.PATROL_BOAT),
                    lastTarget = GridCoordinate(7, 7)
                ),
                onFire = {},
                onRetry = {},
                onPause = {},
                onExit = {}
            )
        }
    }
}

@Preview(
    name = "Victory",
    showBackground = true,
    widthDp = 412,
    heightDp = 915
)
@Composable
private fun VictoryScreenPreview() {
    MobileAndroidApplicationBattleshipProjectTheme(darkTheme = true) {
        Box {
            SeaSceneBackground(mood = SeaSceneMood.VICTORY)
            ResultScreen(
                state = GameUiState(
                    outcome = GameOutcome.VICTORY,
                    statusMessage = "The enemy fleet has been neutralized.",
                    enemyShipsSunk = ShipType.entries.toSet(),
                    outgoingShots = mapOf(
                        GridCoordinate(2, 3) to ShotResult.HIT,
                        GridCoordinate(6, 4) to ShotResult.MISS
                    )
                ),
                onRematch = {},
                onHome = {}
            )
        }
    }
}
