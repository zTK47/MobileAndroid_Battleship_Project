package ch.davide.pham.battleshipproject.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.davide.pham.battleshipproject.model.EnemyFireRequest
import ch.davide.pham.battleshipproject.model.FleetRules
import ch.davide.pham.battleshipproject.model.FireRequest
import ch.davide.pham.battleshipproject.model.GridCoordinate
import ch.davide.pham.battleshipproject.model.JoinRequest
import ch.davide.pham.battleshipproject.model.Orientation
import ch.davide.pham.battleshipproject.model.PlacedShip
import ch.davide.pham.battleshipproject.model.ShipType
import ch.davide.pham.battleshipproject.model.ShotResult
import ch.davide.pham.battleshipproject.model.allSunk
import ch.davide.pham.battleshipproject.model.containsShipAt
import ch.davide.pham.battleshipproject.network.BattleshipRepository
import ch.davide.pham.battleshipproject.network.NetworkResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

/*
 * Author: Davide Pham
 * State and turn sequencing for the official Battleship REST server.
 * Updated to strictly follow the official turn protocol and logging.
 */

enum class AppStage {
    HOME,
    JOIN,
    SETTINGS,
    HOW_TO_PLAY,
    ABOUT,
    FLEET_SETUP,
    CONNECTING,
    BATTLE,
    FINISHED
}

enum class TurnState {
    NONE,
    YOUR_TURN,
    WAITING_FOR_ENEMY
}

enum class ServerStatus {
    CHECKING,
    ONLINE,
    OFFLINE
}

enum class GameOutcome {
    VICTORY,
    DEFEAT
}

data class GameUiState(
    val stage: AppStage = AppStage.HOME,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val playerName: String = "",
    val gameKey: String = "",
    val isHostMode: Boolean = false,
    val serverStatus: ServerStatus = ServerStatus.CHECKING,
    val fleet: List<PlacedShip> = FleetRules.randomFleet(),
    val selectedShip: ShipType = ShipType.CARRIER,
    val placementOrientation: Orientation = Orientation.HORIZONTAL,
    val placementMessage: String = AppLanguage.ENGLISH.strings.positionHelp,
    val incomingShots: Map<GridCoordinate, ShotResult> = emptyMap(),
    val outgoingShots: Map<GridCoordinate, ShotResult> = emptyMap(),
    val enemyShipsSunk: Set<ShipType> = emptySet(),
    val turnState: TurnState = TurnState.NONE,
    val statusTitle: String = AppLanguage.ENGLISH.strings.fleetCommand,
    val statusMessage: String = AppLanguage.ENGLISH.strings.homeTagline,
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
    val outcome: GameOutcome? = null,
    val lastTarget: GridCoordinate? = null,
    val isPaused: Boolean = false,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true
) {
    val canJoinGame: Boolean
        get() = playerName.trim().length >= 3 && gameKey.trim().length >= 3

    val friendlyShipsSunk: Int
        get() = fleet.count { ship ->
            ship.cells().all { incomingShots[it] == ShotResult.HIT }
        }

    val hitCount: Int
        get() = outgoingShots.values.count { it == ShotResult.HIT }

    val accuracy: Int
        get() = if (outgoingShots.isEmpty()) 0 else (hitCount * 100) / outgoingShots.size
}

class BattleshipViewModel(
    private val repository: BattleshipRepository = BattleshipRepository()
) : ViewModel() {
    private val TAG = "BattleshipVM"
    
    var uiState by mutableStateOf(GameUiState())
        private set

    private var sessionJob: Job? = null
    private var connectionAttempt: Long = 0

    init {
        checkServer()
    }

    fun updatePlayerName(value: String) {
        uiState = uiState.copy(playerName = value.take(24), errorMessage = null)
    }

    fun updateGameKey(value: String) {
        uiState = uiState.copy(gameKey = value.trim().uppercase().take(32), errorMessage = null)
    }

    fun generateGameKey() {
        val newKey = UUID.randomUUID().toString().substring(0, 8).uppercase()
        uiState = uiState.copy(gameKey = newKey, errorMessage = null)
    }

    fun openJoin(hostMode: Boolean) {
        uiState = uiState.copy(
            stage = AppStage.JOIN,
            isHostMode = hostMode,
            gameKey = if (hostMode) UUID.randomUUID().toString().substring(0, 8).uppercase() else "",
            errorMessage = null
        )
    }

    fun openSettings() {
        uiState = uiState.copy(stage = AppStage.SETTINGS, errorMessage = null)
    }

    fun openHowToPlay() {
        uiState = uiState.copy(stage = AppStage.HOW_TO_PLAY, errorMessage = null)
    }

    fun openAbout() {
        uiState = uiState.copy(stage = AppStage.ABOUT, errorMessage = null)
    }

    fun setLanguage(language: AppLanguage) {
        uiState = uiState.copy(language = language)
    }

    fun toggleSound() {
        uiState = uiState.copy(soundEnabled = !uiState.soundEnabled)
    }

    fun toggleMusic() {
        uiState = uiState.copy(musicEnabled = !uiState.musicEnabled)
    }

    fun openFleetSetup() {
        if (!uiState.canJoinGame) {
            uiState = uiState.copy(
                errorMessage = uiState.language.strings.playerKeyError
            )
            return
        }
        uiState = uiState.copy(
            stage = AppStage.FLEET_SETUP,
            selectedShip = ShipType.CARRIER,
            placementOrientation = uiState.fleet
                .firstOrNull { it.type == ShipType.CARRIER }
                ?.orientation
                ?: Orientation.HORIZONTAL,
            placementMessage = uiState.language.strings.positionHelp
        )
    }

    fun returnToHome() {
        connectionAttempt++
        sessionJob?.cancel()
        uiState = uiState.copy(
            stage = AppStage.HOME,
            turnState = TurnState.NONE,
            isBusy = false,
            isPaused = false,
            statusTitle = uiState.language.strings.fleetCommand,
            statusMessage = uiState.language.strings.homeTagline,
            errorMessage = null
        )
    }

    fun selectShip(type: ShipType) {
        val ship = uiState.fleet.firstOrNull { it.type == type }
        uiState = uiState.copy(
            selectedShip = type,
            placementOrientation = ship?.orientation ?: uiState.placementOrientation,
            placementMessage = "${type.displayName}: ${uiState.language.strings.positionHelp}"
        )
    }

    fun rotateSelectedShip() {
        val selected = uiState.fleet.firstOrNull { it.type == uiState.selectedShip }
        val newOrientation = uiState.placementOrientation.toggled()
        if (selected == null) {
            uiState = uiState.copy(placementOrientation = newOrientation)
            return
        }

        val moved = FleetRules.placeOrMove(
            fleet = uiState.fleet,
            type = selected.type,
            coordinate = GridCoordinate(selected.x, selected.y),
            orientation = newOrientation
        )
        uiState = if (moved == null) {
            uiState.copy(
                placementOrientation = newOrientation,
                placementMessage = uiState.language.strings.invalidPosition
            )
        } else {
            uiState.copy(
                fleet = moved,
                placementOrientation = newOrientation,
                placementMessage = "${selected.type.displayName}: ${
                    if (newOrientation == Orientation.HORIZONTAL) {
                        uiState.language.strings.rotateHorizontal
                    } else {
                        uiState.language.strings.rotateVertical
                    }
                }"
            )
        }
    }

    fun placeSelectedShip(coordinate: GridCoordinate) {
        val existingShip = FleetRules.shipAt(uiState.fleet, coordinate)
        if (existingShip != null && existingShip.type != uiState.selectedShip) {
            selectShip(existingShip.type)
            return
        }

        val moved = FleetRules.placeOrMove(
            fleet = uiState.fleet,
            type = uiState.selectedShip,
            coordinate = coordinate,
            orientation = uiState.placementOrientation
        )
        uiState = if (moved == null) {
            uiState.copy(
                placementMessage = uiState.language.strings.invalidPosition
            )
        } else {
            uiState.copy(
                fleet = moved,
                placementMessage = "${uiState.selectedShip.displayName}: ${coordinate.label}"
            )
        }
    }

    fun randomizeFleet() {
        val fleet = FleetRules.randomFleet()
        val selected = fleet.first { it.type == uiState.selectedShip }
        uiState = uiState.copy(
            fleet = fleet,
            placementOrientation = selected.orientation,
            placementMessage = uiState.language.strings.randomize
        )
    }

    fun deployFleet() {
        if (!FleetRules.isCompleteAndValid(uiState.fleet)) {
            uiState = uiState.copy(errorMessage = uiState.language.strings.invalidPosition)
            return
        }

        sessionJob?.cancel()
        val attempt = ++connectionAttempt
        val player = uiState.playerName.trim()
        val gameKey = uiState.gameKey.trim()
        val ships = uiState.fleet.map { it.toNetworkModel() }
        uiState = uiState.copy(
            stage = AppStage.CONNECTING,
            isBusy = true,
            turnState = TurnState.NONE,
            statusTitle = uiState.language.strings.connectingTitle,
            statusMessage = uiState.language.strings.connectingMessage,
            incomingShots = emptyMap(),
            outgoingShots = emptyMap(),
            enemyShipsSunk = emptySet(),
            outcome = null,
            lastTarget = null,
            isPaused = false,
            errorMessage = null
        )

        sessionJob = viewModelScope.launch {
            val request = JoinRequest(
                player = player,
                gameKey = gameKey,
                ships = ships
            )
            Log.d(TAG, "Executing JOIN for $player on $gameKey")
            when (val result = repository.join(request)) {
                is NetworkResult.Failure -> {
                    if (!isCurrentConnection(attempt, player, gameKey)) return@launch
                    Log.d(TAG, "JOIN failed: ${result.message}")
                    uiState = uiState.copy(
                        stage = AppStage.FLEET_SETUP,
                        isBusy = false,
                        errorMessage = localizedNetworkError(result.message),
                        placementMessage = uiState.language.strings.serverOffline
                    )
                }

                is NetworkResult.Success -> {
                    if (!isCurrentConnection(attempt, player, gameKey)) return@launch
                    val response = result.value
                    Log.d(TAG, "JOIN success. Enemy shot: ${response.x},${response.y}, gameover: ${response.gameOver}")
                    
                    val coordinate = response.x?.let { x ->
                        response.y?.let { y -> GridCoordinate(x, y) }
                    }
                    if (coordinate != null) applyIncomingShot(coordinate)

                    if (response.gameOver) {
                        finishGame(if (coordinate != null) GameOutcome.DEFEAT else GameOutcome.VICTORY)
                    } else {
                        // Protocol: After join, it is always your turn. 
                        // Either you are first (x/y null) or you just received the first player's move.
                        uiState = uiState.copy(
                            stage = AppStage.BATTLE,
                            isBusy = false,
                            turnState = TurnState.YOUR_TURN,
                            statusTitle = uiState.language.strings.yourTurn,
                            statusMessage = if (coordinate == null) {
                                uiState.language.strings.yourTurnMessage
                            } else {
                                "${uiState.language.strings.incomingFire}: ${coordinate.label}"
                            }
                        )
                    }
                }
            }
        }
    }

    fun cancelConnection() {
        connectionAttempt++
        sessionJob?.cancel()
        uiState = uiState.copy(
            stage = AppStage.FLEET_SETUP,
            isBusy = false,
            placementMessage = uiState.language.strings.cancelConnection
        )
    }

    fun fireAt(coordinate: GridCoordinate) {
        if (
            uiState.stage != AppStage.BATTLE ||
            uiState.turnState != TurnState.YOUR_TURN ||
            uiState.isBusy ||
            uiState.isPaused
        ) {
            return
        }
        if (coordinate in uiState.outgoingShots) {
            uiState = uiState.copy(
                errorMessage = "${uiState.language.strings.repeatedTarget} (${coordinate.label})"
            )
            return
        }

        sessionJob = viewModelScope.launch {
            uiState = uiState.copy(
                isBusy = true,
                statusTitle = uiState.language.strings.yourTurn,
                statusMessage = "${uiState.language.strings.targetingGrid}: ${coordinate.label}",
                errorMessage = null
            )
            val request = FireRequest(
                player = uiState.playerName.trim(),
                gameKey = uiState.gameKey.trim(),
                x = coordinate.x,
                y = coordinate.y
            )
            Log.d(TAG, "Executing FIRE at ${coordinate.label}")
            when (val result = repository.fire(request)) {
                is NetworkResult.Failure -> {
                    Log.d(TAG, "FIRE failed: ${result.message}")
                    uiState = uiState.copy(
                        isBusy = false,
                        turnState = TurnState.YOUR_TURN,
                        statusTitle = uiState.language.strings.commandNotice,
                        statusMessage = uiState.language.strings.yourTurnMessage,
                        errorMessage = localizedNetworkError(result.message)
                    )
                }

                is NetworkResult.Success -> {
                    val response = result.value
                    Log.d(TAG, "FIRE success. Hit: ${response.hit}, gameover: ${response.gameOver}")
                    val shotResult = if (response.hit) ShotResult.HIT else ShotResult.MISS
                    val sunkShips = response.shipsSunk
                        .mapNotNull(ShipType::fromApiName)
                        .toSet()
                    
                    uiState = uiState.copy(
                        outgoingShots = uiState.outgoingShots + (coordinate to shotResult),
                        enemyShipsSunk = uiState.enemyShipsSunk + sunkShips,
                        lastTarget = coordinate,
                        statusTitle = if (response.hit) {
                            uiState.language.strings.hit
                        } else {
                            uiState.language.strings.miss
                        },
                        statusMessage = if (response.hit) {
                            "${uiState.language.strings.hit}: ${coordinate.label}"
                        } else {
                            "${uiState.language.strings.miss}: ${coordinate.label}"
                        }
                    )

                    if (response.gameOver) {
                        finishGame(GameOutcome.VICTORY)
                    } else {
                        uiState = uiState.copy(turnState = TurnState.WAITING_FOR_ENEMY)
                        awaitEnemyMove()
                    }
                }
            }
        }
    }

    fun retryEnemyMove() {
        if (
            uiState.stage == AppStage.BATTLE &&
            uiState.turnState == TurnState.WAITING_FOR_ENEMY &&
            !uiState.isBusy
        ) {
            sessionJob = viewModelScope.launch {
                uiState = uiState.copy(errorMessage = null)
                awaitEnemyMove()
            }
        }
    }

    fun dismissError() {
        uiState = uiState.copy(errorMessage = null)
    }

    fun togglePause() {
        if (uiState.stage != AppStage.BATTLE) return
        uiState = uiState.copy(isPaused = !uiState.isPaused)
    }

    fun prepareRematch() {
        val fleet = FleetRules.randomFleet()
        uiState = uiState.copy(
            stage = AppStage.FLEET_SETUP,
            fleet = fleet,
            selectedShip = ShipType.CARRIER,
            placementOrientation = fleet.first().orientation,
            placementMessage = uiState.language.strings.positionHelp,
            incomingShots = emptyMap(),
            outgoingShots = emptyMap(),
            enemyShipsSunk = emptySet(),
            turnState = TurnState.NONE,
            isBusy = false,
            outcome = null,
            lastTarget = null,
            isPaused = false,
            errorMessage = null
        )
    }

    private suspend fun awaitEnemyMove() {
        uiState = uiState.copy(
            isBusy = true,
            turnState = TurnState.WAITING_FOR_ENEMY,
            statusTitle = uiState.language.strings.waitingEnemy,
            statusMessage = uiState.language.strings.waitingEnemyMessage
        )
        val request = EnemyFireRequest(
            player = uiState.playerName.trim(),
            gameKey = uiState.gameKey.trim()
        )
        Log.d(TAG, "Executing ENEMY_FIRE (long poll)")
        when (val result = repository.enemyFire(request)) {
            is NetworkResult.Failure -> {
                Log.d(TAG, "ENEMY_FIRE failed: ${result.message}")
                uiState = uiState.copy(
                    isBusy = false,
                    statusTitle = uiState.language.strings.commandNotice,
                    statusMessage = uiState.language.strings.waitingEnemyMessage,
                    errorMessage = localizedNetworkError(result.message)
                )
            }

            is NetworkResult.Success -> {
                val response = result.value
                Log.d(TAG, "ENEMY_FIRE success. Enemy shot: ${response.x},${response.y}, gameover: ${response.gameOver}")
                val coordinate = response.x?.let { x ->
                    response.y?.let { y -> GridCoordinate(x, y) }
                }
                if (coordinate != null) {
                    applyIncomingShot(coordinate)
                }

                if (response.gameOver) {
                    finishGame(GameOutcome.DEFEAT)
                } else {
                    uiState = uiState.copy(
                        isBusy = false,
                        turnState = TurnState.YOUR_TURN,
                        statusTitle = uiState.language.strings.yourTurn,
                        statusMessage = if (coordinate == null) {
                            uiState.language.strings.yourTurnMessage
                        } else {
                            val hit = uiState.incomingShots[coordinate] == ShotResult.HIT
                            if (hit) {
                                "${uiState.language.strings.hit}: ${coordinate.label}"
                            } else {
                                "${uiState.language.strings.miss}: ${coordinate.label}"
                            }
                        }
                    )
                }
            }
        }
    }

    private fun applyIncomingShot(coordinate: GridCoordinate) {
        if (!coordinate.isOnBoard) return
        val result = if (uiState.fleet.containsShipAt(coordinate)) {
            ShotResult.HIT
        } else {
            ShotResult.MISS
        }
        uiState = uiState.copy(
            incomingShots = uiState.incomingShots + (coordinate to result)
        )
    }

    private fun finishGame(outcome: GameOutcome) {
        Log.d(TAG, "Game Finished. Outcome: $outcome")
        uiState = uiState.copy(
            stage = AppStage.FINISHED,
            turnState = TurnState.NONE,
            isBusy = false,
            isPaused = false,
            outcome = outcome,
            statusTitle = if (outcome == GameOutcome.VICTORY) {
                uiState.language.strings.victory
            } else {
                uiState.language.strings.defeat
            },
            statusMessage = if (outcome == GameOutcome.VICTORY) {
                uiState.language.strings.victoryMessage
            } else {
                uiState.language.strings.defeatMessage
            }
        )
    }

    private fun localizedNetworkError(message: String): String =
        when {
            message.startsWith("Cannot reach the Battleship server") ->
                uiState.language.strings.serverOffline
            message.startsWith("The server returned an empty response") ->
                uiState.language.strings.commandNotice
            else -> message
        }

    private fun isCurrentConnection(
        attempt: Long,
        player: String,
        gameKey: String
    ): Boolean =
        connectionAttempt == attempt &&
            uiState.stage == AppStage.CONNECTING &&
            uiState.playerName.trim() == player &&
            uiState.gameKey.trim() == gameKey

    fun checkServer() {
        viewModelScope.launch {
            uiState = uiState.copy(serverStatus = ServerStatus.CHECKING)
            uiState = when (val result = repository.ping()) {
                is NetworkResult.Success -> uiState.copy(
                    serverStatus = if (result.value.ping) ServerStatus.ONLINE else ServerStatus.OFFLINE
                )

                is NetworkResult.Failure -> uiState.copy(serverStatus = ServerStatus.OFFLINE)
            }
        }
    }
}
