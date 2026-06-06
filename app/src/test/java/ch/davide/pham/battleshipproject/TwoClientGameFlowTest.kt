package ch.davide.pham.battleshipproject

import ch.davide.pham.battleshipproject.model.EnemyFireRequest
import ch.davide.pham.battleshipproject.model.EnemyFireResponse
import ch.davide.pham.battleshipproject.model.FireRequest
import ch.davide.pham.battleshipproject.model.FireResponse
import ch.davide.pham.battleshipproject.model.GridCoordinate
import ch.davide.pham.battleshipproject.model.JoinRequest
import ch.davide.pham.battleshipproject.model.JoinResponse
import ch.davide.pham.battleshipproject.model.PingResponse
import ch.davide.pham.battleshipproject.model.ShipPosition
import ch.davide.pham.battleshipproject.model.ShotResult
import ch.davide.pham.battleshipproject.network.BattleshipApi
import ch.davide.pham.battleshipproject.network.BattleshipRepository
import ch.davide.pham.battleshipproject.network.NetworkResult
import ch.davide.pham.battleshipproject.ui.AppStage
import ch.davide.pham.battleshipproject.ui.BattleshipViewModel
import ch.davide.pham.battleshipproject.ui.GameOutcome
import ch.davide.pham.battleshipproject.ui.TurnState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class TwoClientGameFlowTest {
    @get:Rule
    val mainDispatcherRule: TestWatcher = MainDispatcherRule()

    @Test
    fun clientsUsingSameKeyAlternateTurnsAndReceiveShots() = runTest {
        val server = SharedFakeBattleServer()
        val host = BattleshipViewModel(FakeRepository(server))
        val guest = BattleshipViewModel(FakeRepository(server))
        advanceUntilIdle()

        val gameKey = "DUEL-2026"
        configurePlayer(host, "Alpha", gameKey, hostMode = true)
        configurePlayer(guest, "Bravo", gameKey, hostMode = false)

        host.deployFleet()
        guest.deployFleet()
        advanceUntilIdle()

        assertEquals(AppStage.BATTLE, host.uiState.stage)
        assertEquals(TurnState.YOUR_TURN, host.uiState.turnState)
        assertEquals(AppStage.CONNECTING, guest.uiState.stage)

        val guestShipCell = guest.uiState.fleet.first().cells().first()
        host.fireAt(guestShipCell)
        advanceUntilIdle()

        assertEquals(ShotResult.HIT, host.uiState.outgoingShots[guestShipCell])
        assertEquals(ShotResult.HIT, guest.uiState.incomingShots[guestShipCell])
        assertEquals(TurnState.WAITING_FOR_ENEMY, host.uiState.turnState)
        assertEquals(TurnState.YOUR_TURN, guest.uiState.turnState)

        val hostWaterCell = boardCoordinates().first { coordinate ->
            host.uiState.fleet.none { coordinate in it.cells() }
        }
        guest.fireAt(hostWaterCell)
        advanceUntilIdle()

        assertEquals(ShotResult.MISS, guest.uiState.outgoingShots[hostWaterCell])
        assertEquals(ShotResult.MISS, host.uiState.incomingShots[hostWaterCell])
        assertEquals(TurnState.WAITING_FOR_ENEMY, guest.uiState.turnState)
        assertEquals(TurnState.YOUR_TURN, host.uiState.turnState)

        host.returnToHome()
        guest.returnToHome()
        advanceUntilIdle()
    }

    @Test
    fun clientsUsingSameKeyCanFinishACompleteGame() = runTest {
        val server = SharedFakeBattleServer()
        val host = BattleshipViewModel(FakeRepository(server))
        val guest = BattleshipViewModel(FakeRepository(server))
        advanceUntilIdle()

        val gameKey = "FULL-GAME-2026"
        configurePlayer(host, "Alpha", gameKey, hostMode = true)
        configurePlayer(guest, "Bravo", gameKey, hostMode = false)
        host.deployFleet()
        guest.deployFleet()
        advanceUntilIdle()

        val guestFleetCells = guest.uiState.fleet.flatMap { it.cells() }
        val hostWaterCells = boardCoordinates()
            .filter { coordinate -> host.uiState.fleet.none { coordinate in it.cells() } }
            .iterator()

        guestFleetCells.forEachIndexed { index, target ->
            host.fireAt(target)
            advanceUntilIdle()

            if (index < guestFleetCells.lastIndex) {
                assertEquals(TurnState.YOUR_TURN, guest.uiState.turnState)
                guest.fireAt(hostWaterCells.next())
                advanceUntilIdle()
                assertEquals(TurnState.YOUR_TURN, host.uiState.turnState)
            }
        }

        assertEquals(AppStage.FINISHED, host.uiState.stage)
        assertEquals(GameOutcome.VICTORY, host.uiState.outcome)
        assertEquals(AppStage.FINISHED, guest.uiState.stage)
        assertEquals(GameOutcome.DEFEAT, guest.uiState.outcome)
        assertEquals(17, host.uiState.outgoingShots.size)
        assertEquals(17, guest.uiState.incomingShots.size)
    }

    @Test
    fun clientsUsingDifferentKeysRemainInSeparateWaitingRooms() = runTest {
        val server = SharedFakeBattleServer()
        val host = BattleshipViewModel(FakeRepository(server))
        val unrelatedGuest = BattleshipViewModel(FakeRepository(server))
        advanceUntilIdle()

        configurePlayer(host, "Alpha", "ROOM-ALPHA", hostMode = true)
        configurePlayer(unrelatedGuest, "Bravo", "ROOM-BRAVO", hostMode = false)
        host.deployFleet()
        unrelatedGuest.deployFleet()
        advanceUntilIdle()

        assertEquals(AppStage.CONNECTING, host.uiState.stage)
        assertEquals(AppStage.CONNECTING, unrelatedGuest.uiState.stage)
        assertEquals(TurnState.NONE, host.uiState.turnState)
        assertEquals(TurnState.NONE, unrelatedGuest.uiState.turnState)

        host.cancelConnection()
        unrelatedGuest.cancelConnection()
        advanceUntilIdle()
    }

    @Test
    fun secondClientCanBeRandomlySelectedToStart() = runTest {
        val server = SharedFakeBattleServer(startingPlayerIndex = 1)
        val host = BattleshipViewModel(FakeRepository(server))
        val guest = BattleshipViewModel(FakeRepository(server))
        advanceUntilIdle()

        val gameKey = "GUEST-STARTS"
        configurePlayer(host, "Alpha", gameKey, hostMode = true)
        configurePlayer(guest, "Bravo", gameKey, hostMode = false)
        host.deployFleet()
        guest.deployFleet()
        advanceUntilIdle()

        assertEquals(AppStage.CONNECTING, host.uiState.stage)
        assertEquals(AppStage.BATTLE, guest.uiState.stage)
        assertEquals(TurnState.YOUR_TURN, guest.uiState.turnState)

        val hostShipCell = host.uiState.fleet.first().cells().first()
        guest.fireAt(hostShipCell)
        advanceUntilIdle()

        assertEquals(ShotResult.HIT, host.uiState.incomingShots[hostShipCell])
        assertEquals(AppStage.BATTLE, host.uiState.stage)
        assertEquals(TurnState.YOUR_TURN, host.uiState.turnState)
        assertEquals(TurnState.WAITING_FOR_ENEMY, guest.uiState.turnState)

        host.returnToHome()
        guest.returnToHome()
        advanceUntilIdle()
    }

    @Test
    fun lateResponseFromCancelledRoomCannotStartTheWrongGame() = runTest {
        val repository = DelayedJoinRepository()
        val player = BattleshipViewModel(repository)
        advanceUntilIdle()

        configurePlayer(player, "Alpha", "OLD-ROOM", hostMode = true)
        player.deployFleet()
        advanceUntilIdle()

        player.cancelConnection()
        configurePlayer(player, "Alpha", "NEW-ROOM", hostMode = false)
        player.deployFleet()
        advanceUntilIdle()

        repository.joinResults[0].complete(
            NetworkResult.Success(JoinResponse(gameOver = false))
        )
        advanceUntilIdle()

        assertEquals(AppStage.CONNECTING, player.uiState.stage)
        assertEquals("NEW-ROOM", player.uiState.gameKey)

        repository.joinResults[1].complete(
            NetworkResult.Success(JoinResponse(gameOver = false))
        )
        advanceUntilIdle()

        assertEquals(AppStage.BATTLE, player.uiState.stage)
        assertEquals("NEW-ROOM", player.uiState.gameKey)
    }

    private fun configurePlayer(
        viewModel: BattleshipViewModel,
        player: String,
        gameKey: String,
        hostMode: Boolean
    ) {
        viewModel.openJoin(hostMode)
        viewModel.updatePlayerName(player)
        viewModel.updateGameKey(gameKey)
        viewModel.openFleetSetup()
        assertEquals(AppStage.FLEET_SETUP, viewModel.uiState.stage)
    }

    private fun boardCoordinates(): Sequence<GridCoordinate> =
        sequence {
            for (y in 0 until 10) {
                for (x in 0 until 10) {
                    yield(GridCoordinate(x, y))
                }
            }
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
private class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class FakeRepository(
    private val server: SharedFakeBattleServer
) : BattleshipRepository(NoOpApi) {
    override suspend fun ping(): NetworkResult<PingResponse> =
        NetworkResult.Success(PingResponse(true))

    override suspend fun join(request: JoinRequest): NetworkResult<JoinResponse> =
        NetworkResult.Success(server.join(request))

    override suspend fun fire(request: FireRequest): NetworkResult<FireResponse> =
        NetworkResult.Success(server.fire(request))

    override suspend fun enemyFire(
        request: EnemyFireRequest
    ): NetworkResult<EnemyFireResponse> =
        NetworkResult.Success(server.enemyFire(request))
}

private class DelayedJoinRepository : BattleshipRepository(NoOpApi) {
    val joinResults = mutableListOf<CompletableDeferred<NetworkResult<JoinResponse>>>()

    override suspend fun ping(): NetworkResult<PingResponse> =
        NetworkResult.Success(PingResponse(true))

    override suspend fun join(request: JoinRequest): NetworkResult<JoinResponse> {
        val result = CompletableDeferred<NetworkResult<JoinResponse>>()
        joinResults += result
        return withContext(NonCancellable) { result.await() }
    }
}

private object NoOpApi : BattleshipApi {
    override suspend fun ping(): Response<PingResponse> = error("Not used")
    override suspend fun joinGame(request: JoinRequest): Response<JoinResponse> = error("Not used")
    override suspend fun fire(request: FireRequest): Response<FireResponse> = error("Not used")
    override suspend fun enemyFire(
        request: EnemyFireRequest
    ): Response<EnemyFireResponse> = error("Not used")
}

private class SharedFakeBattleServer(
    private val startingPlayerIndex: Int = 0
) {
    private data class PlayerState(
        val ships: List<ShipPosition>,
        val receivedShots: MutableSet<GridCoordinate> = mutableSetOf()
    )

    private data class GameState(
        val players: LinkedHashMap<String, PlayerState> = linkedMapOf(),
        val joinWaiters: MutableMap<String, CompletableDeferred<JoinResponse>> = mutableMapOf(),
        val fireWaiters: MutableMap<String, CompletableDeferred<EnemyFireResponse>> = mutableMapOf(),
        val pendingShots: MutableMap<String, GridCoordinate> = mutableMapOf(),
        val pendingGameOver: MutableSet<String> = mutableSetOf()
    )

    private val mutex = Mutex()
    private val games = mutableMapOf<String, GameState>()

    suspend fun join(request: JoinRequest): JoinResponse {
        val waiter = CompletableDeferred<JoinResponse>()
        mutex.withLock {
            val game = games.getOrPut(request.gameKey) { GameState() }
            game.players[request.player] = PlayerState(request.ships)
            game.joinWaiters[request.player] = waiter
            if (game.players.size == 2) {
                val starter = game.players.keys.elementAt(startingPlayerIndex.coerceIn(0, 1))
                game.joinWaiters.remove(starter)?.complete(
                    JoinResponse(gameOver = false)
                )
            }
        }
        return waiter.await()
    }

    suspend fun fire(request: FireRequest): FireResponse {
        val coordinate = GridCoordinate(request.x, request.y)
        lateinit var response: FireResponse
        mutex.withLock {
            val game = games.getValue(request.gameKey)
            val opponent = game.players.keys.first { it != request.player }
            val opponentState = game.players.getValue(opponent)
            opponentState.receivedShots += coordinate

            val hit = opponentState.ships.any { coordinate in it.cells() }
            val sunk = opponentState.ships
                .filter { ship -> ship.cells().all(opponentState.receivedShots::contains) }
                .map(ShipPosition::ship)
            val gameOver = sunk.size == opponentState.ships.size
            response = FireResponse(hit = hit, shipsSunk = sunk)

            val joinWaiter = game.joinWaiters.remove(opponent)
            val fireWaiter = game.fireWaiters.remove(opponent)
            when {
                joinWaiter != null -> joinWaiter.complete(
                    JoinResponse(
                        x = coordinate.x,
                        y = coordinate.y,
                        gameOver = gameOver
                    )
                )

                fireWaiter != null -> fireWaiter.complete(
                    EnemyFireResponse(
                        x = coordinate.x,
                        y = coordinate.y,
                        gameOver = gameOver
                    )
                )

                else -> game.pendingShots[opponent] = coordinate
            }
            if (gameOver) {
                game.pendingGameOver += request.player
            }
        }
        return response
    }

    suspend fun enemyFire(request: EnemyFireRequest): EnemyFireResponse {
        val waiter = CompletableDeferred<EnemyFireResponse>()
        mutex.withLock {
            val game = games.getValue(request.gameKey)
            when {
                game.pendingGameOver.remove(request.player) -> waiter.complete(
                    EnemyFireResponse(gameOver = true)
                )

                game.pendingShots.containsKey(request.player) -> {
                    val pending = game.pendingShots.remove(request.player)!!
                    waiter.complete(
                        EnemyFireResponse(
                            x = pending.x,
                            y = pending.y,
                            gameOver = false
                        )
                    )
                }

                else -> {
                    game.fireWaiters[request.player] = waiter
                }
            }
        }
        return waiter.await()
    }

    private fun ShipPosition.cells(): List<GridCoordinate> =
        List(shipLength(ship)) { index ->
            GridCoordinate(
                x = if (orientation == "horizontal") x + index else x,
                y = if (orientation == "vertical") y + index else y
            )
        }

    private fun shipLength(ship: String): Int =
        when (ship) {
            "Carrier" -> 5
            "Battleship" -> 4
            "Destroyer", "Submarine" -> 3
            "PatrolBoat" -> 2
            else -> error("Unknown ship: $ship")
        }
}
