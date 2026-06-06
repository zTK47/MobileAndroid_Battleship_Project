package ch.davide.pham.battleshipproject.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.davide.pham.battleshipproject.model.FleetRules
import ch.davide.pham.battleshipproject.model.GridCoordinate
import ch.davide.pham.battleshipproject.model.Orientation
import ch.davide.pham.battleshipproject.model.PlacedShip
import ch.davide.pham.battleshipproject.model.ShipType
import ch.davide.pham.battleshipproject.model.ShotResult
import ch.davide.pham.battleshipproject.ui.GameOutcome
import ch.davide.pham.battleshipproject.ui.GameUiState
import ch.davide.pham.battleshipproject.ui.TurnState
import ch.davide.pham.battleshipproject.ui.strings
import ch.davide.pham.battleshipproject.ui.components.BattleBoard
import ch.davide.pham.battleshipproject.ui.components.FleetStatusList
import ch.davide.pham.battleshipproject.ui.components.GlassPanel
import ch.davide.pham.battleshipproject.ui.components.RadarLoader
import ch.davide.pham.battleshipproject.ui.components.SectionLabel
import ch.davide.pham.battleshipproject.ui.components.ShipSelector
import ch.davide.pham.battleshipproject.ui.components.StatTile
import ch.davide.pham.battleshipproject.ui.theme.DeepNavy
import ch.davide.pham.battleshipproject.ui.theme.ImpactCoral
import ch.davide.pham.battleshipproject.ui.theme.SignalAmber
import ch.davide.pham.battleshipproject.ui.theme.SonarCyan
import ch.davide.pham.battleshipproject.ui.theme.SuccessMint

/*
 * Author: Davide Pham (individual FHNW student project).
 * OpenAI Codex assisted with implementation and visual design.
 */
@Composable
fun FleetSetupScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onSelectShip: (ShipType) -> Unit,
    onRotate: () -> Unit,
    onRandomize: () -> Unit,
    onPlace: (GridCoordinate) -> Unit,
    onDeploy: () -> Unit
) {
    val text = state.language.strings
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CommandTopBar(
                title = text.fleetDeployment,
                subtitle = "FORMATION 05/05",
                onBack = onBack,
                backLabel = text.backToMenu
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .safeDrawingPadding()
        ) {
            val wide = maxWidth >= 840.dp
            val content = Modifier
                .fillMaxWidth()
                .widthIn(max = 1180.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)

            if (wide) {
                Row(
                    modifier = content,
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    FleetBoardPanel(state, onPlace, Modifier.weight(1.2f))
                    FleetControls(
                        state,
                        onSelectShip,
                        onRotate,
                        onRandomize,
                        onDeploy,
                        Modifier.weight(0.9f)
                    )
                }
            } else {
                Column(
                    modifier = content,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    FleetBoardPanel(state, onPlace)
                    FleetControls(
                        state,
                        onSelectShip,
                        onRotate,
                        onRandomize,
                        onDeploy
                    )
                }
            }
        }
    }
}

@Composable
private fun FleetBoardPanel(
    state: GameUiState,
    onPlace: (GridCoordinate) -> Unit,
    modifier: Modifier = Modifier
) {
    val text = state.language.strings
    GlassPanel(modifier) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionLabel(
                eyebrow = "FRIENDLY WATERS",
                title = text.positionFleet,
                description = text.positionHelp
            )
            BattleBoard(
                fleet = state.fleet,
                shots = emptyMap(),
                showFleet = true,
                enabled = true,
                selectedShip = state.selectedShip,
                onCellTap = onPlace,
                modifier = Modifier.fillMaxWidth()
            )
            Surface(
                color = SignalAmber.copy(alpha = 0.1f),
                contentColor = SignalAmber,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    state.placementMessage,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun FleetControls(
    state: GameUiState,
    onSelectShip: (ShipType) -> Unit,
    onRotate: () -> Unit,
    onRandomize: () -> Unit,
    onDeploy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val text = state.language.strings
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GlassPanel {
            Column(
                Modifier.padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                SectionLabel(
                    eyebrow = "VESSEL CONTROL",
                    title = state.selectedShip.displayName,
                    description = "Length ${state.selectedShip.size} sectors",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
                ) {
                    items(state.fleet.sortedBy { it.type.ordinal }) { ship ->
                        ShipSelector(
                            ship = ship,
                            selected = ship.type == state.selectedShip,
                            onClick = { onSelectShip(ship.type) }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(onClick = onRotate, modifier = Modifier.weight(1f)) {
                        Text(
                            if (state.placementOrientation == Orientation.HORIZONTAL) {
                                text.rotateVertical
                            } else {
                                text.rotateHorizontal
                            }
                        )
                    }
                    OutlinedButton(onClick = onRandomize, modifier = Modifier.weight(1f)) {
                        Text(text.randomize)
                    }
                }
            }
        }
        Button(
            onClick = onDeploy,
            enabled = FleetRules.isCompleteAndValid(state.fleet),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SuccessMint,
                contentColor = DeepNavy
            )
        ) {
            Text(text.deployFleet)
        }
        Text(
            text.onlineJoinHelp,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ConnectingScreen(
    state: GameUiState,
    onCancel: () -> Unit,
    onHome: () -> Unit
) {
    val text = state.language.strings
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassPanel(Modifier.widthIn(max = 500.dp)) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                RadarLoader()
                Text(
                    text.connectingTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text.connectingMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
                Text(
                    "POST  brad-home.ch:50003/game/join",
                    style = MaterialTheme.typography.labelMedium,
                    color = SonarCyan
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TacticalChip("CMD ${state.playerName.uppercase()}")
                    TacticalChip("KEY ${state.gameKey}")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                        Text(text.cancelConnection)
                    }
                    Button(onClick = onHome, modifier = Modifier.weight(1f)) {
                        Text(text.backToMenu)
                    }
                }
            }
        }
    }
}

@Composable
fun BattleScreen(
    state: GameUiState,
    onFire: (GridCoordinate) -> Unit,
    onRetry: () -> Unit,
    onPause: () -> Unit,
    onExit: () -> Unit
) {
    val text = state.language.strings
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CommandTopBar(
                title = text.battleTitle,
                subtitle = "KEY ${state.gameKey}",
                onBack = onExit,
                backLabel = text.backToMenu
            )
        },
        bottomBar = {
            Surface(
                color = DeepNavy.copy(alpha = 0.97f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .safeDrawingPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = onPause,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (state.isPaused) text.resume else text.pause)
                    }
                    Button(
                        onClick = onExit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ImpactCoral,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text.quitBattle)
                    }
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .safeDrawingPadding()
        ) {
            val wide = maxWidth >= 900.dp
            val content = Modifier
                .fillMaxWidth()
                .widthIn(max = 1300.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)

            Column(content, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TurnStatusPanel(state, onRetry)
                if (wide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        BattleGridPanel(
                            title = text.targetingGrid,
                            description = text.targetHint,
                            fleet = emptyList(),
                            shots = state.outgoingShots,
                            showFleet = false,
                            enabled = state.turnState == TurnState.YOUR_TURN &&
                                !state.isBusy &&
                                !state.isPaused,
                            highlightedCoordinate = state.lastTarget,
                            weaponsLabel = text.weaponsFree,
                            onTap = onFire,
                            modifier = Modifier.weight(1f)
                        )
                        BattleGridPanel(
                            title = text.friendlyGrid,
                            description = text.incomingFire,
                            fleet = state.fleet,
                            shots = state.incomingShots,
                            showFleet = true,
                            enabled = false,
                            weaponsLabel = text.weaponsFree,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    BattleGridPanel(
                        title = text.targetingGrid,
                        description = text.targetHint,
                        fleet = emptyList(),
                        shots = state.outgoingShots,
                        showFleet = false,
                        enabled = state.turnState == TurnState.YOUR_TURN &&
                            !state.isBusy &&
                            !state.isPaused,
                        highlightedCoordinate = state.lastTarget,
                        weaponsLabel = text.weaponsFree,
                        onTap = onFire
                    )
                    BattleGridPanel(
                        title = text.friendlyGrid,
                        description = text.incomingFire,
                        fleet = state.fleet,
                        shots = state.incomingShots,
                        showFleet = true,
                        enabled = false,
                        weaponsLabel = text.weaponsFree
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatTile(
                        label = text.salvos,
                        value = state.outgoingShots.size.toString().padStart(2, '0'),
                        accent = SonarCyan,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = text.accuracy,
                        value = "${state.accuracy}%",
                        accent = SignalAmber,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = text.enemySunk,
                        value = "${state.enemyShipsSunk.size}/5",
                        accent = ImpactCoral,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (wide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FleetStatusList(
                            title = text.friendlyFleet,
                            fleet = state.fleet,
                            incomingShots = state.incomingShots,
                            modifier = Modifier.weight(1f)
                        )
                        FleetStatusList(
                            title = text.enemyFleet,
                            fleet = state.fleet,
                            incomingShots = emptyMap(),
                            enemySunk = state.enemyShipsSunk,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    FleetStatusList(
                        title = text.friendlyFleet,
                        fleet = state.fleet,
                        incomingShots = state.incomingShots
                    )
                    FleetStatusList(
                        title = text.enemyFleet,
                        fleet = state.fleet,
                        incomingShots = emptyMap(),
                        enemySunk = state.enemyShipsSunk
                    )
                }
                BoardLegend(text.miss, text.hit, text.selectedVessel)
            }

            if (state.isPaused) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(30.dp)
                        .widthIn(max = 460.dp),
                    color = DeepNavy.copy(alpha = 0.97f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = MaterialTheme.shapes.large,
                    shadowElevation = 18.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadarLoader()
                        Text(
                            text.pausedTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            color = SignalAmber
                        )
                        Text(text.pausedMessage, style = MaterialTheme.typography.bodyLarge)
                        Button(onClick = onPause, modifier = Modifier.fillMaxWidth()) {
                            Text(text.resume)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TurnStatusPanel(state: GameUiState, onRetry: () -> Unit) {
    val text = state.language.strings
    val accent = when (state.turnState) {
        TurnState.YOUR_TURN -> SuccessMint
        TurnState.WAITING_FOR_ENEMY -> SignalAmber
        TurnState.NONE -> SonarCyan
    }
    GlassPanel(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            if (state.isBusy || state.turnState == TurnState.WAITING_FOR_ENEMY) {
                RadarLoader(compact = true)
            } else {
                Box(
                    Modifier
                        .size(34.dp)
                        .background(accent.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(9.dp).background(accent, CircleShape))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    if (state.turnState == TurnState.YOUR_TURN) text.yourTurn
                    else if (state.turnState == TurnState.WAITING_FOR_ENEMY) text.waitingEnemy
                    else state.statusTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = accent
                )
                Text(
                    if (state.turnState == TurnState.YOUR_TURN) text.yourTurnMessage
                    else if (state.turnState == TurnState.WAITING_FOR_ENEMY) text.waitingEnemyMessage
                    else state.statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            if (state.turnState == TurnState.WAITING_FOR_ENEMY && !state.isBusy) {
                Button(onClick = onRetry) { Text(text.reconnect) }
            }
        }
    }
}

@Composable
private fun BattleGridPanel(
    title: String,
    description: String,
    fleet: List<PlacedShip>,
    shots: Map<GridCoordinate, ShotResult>,
    showFleet: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    highlightedCoordinate: GridCoordinate? = null,
    weaponsLabel: String = "WEAPONS FREE",
    onTap: (GridCoordinate) -> Unit = {}
) {
    GlassPanel(modifier) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(title, style = MaterialTheme.typography.labelLarge, color = SonarCyan)
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                    )
                }
                if (enabled) TacticalChip(weaponsLabel, SuccessMint)
            }
            BattleBoard(
                fleet = fleet,
                shots = shots,
                showFleet = showFleet,
                enabled = enabled,
                highlightedCoordinate = highlightedCoordinate,
                onCellTap = onTap,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ResultScreen(
    state: GameUiState,
    onRematch: () -> Unit,
    onHome: () -> Unit
) {
    val text = state.language.strings
    val victory = state.outcome == GameOutcome.VICTORY
    val accent = if (victory) SuccessMint else ImpactCoral
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassPanel(Modifier.widthIn(max = 620.dp)) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                ResultEmblem(victory)
                Text(
                    if (victory) text.victory else text.defeat,
                    style = MaterialTheme.typography.displaySmall,
                    color = accent
                )
                Text(
                    if (victory) text.victoryMessage else text.defeatMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatTile(
                        label = text.salvos,
                        value = state.outgoingShots.size.toString(),
                        accent = SonarCyan,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = text.accuracy,
                        value = "${state.accuracy}%",
                        accent = SignalAmber,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = text.enemySunk,
                        value = state.enemyShipsSunk.size.toString(),
                        accent = ImpactCoral,
                        modifier = Modifier.weight(1f)
                    )
                }
                Button(
                    onClick = onRematch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = DeepNavy
                    )
                ) {
                    Text(text.playAgain)
                }
                TextButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) {
                    Text(text.returnHome)
                }
            }
        }
    }
}

@Composable
private fun ResultEmblem(victory: Boolean) {
    val accent = if (victory) SuccessMint else ImpactCoral
    Canvas(Modifier.size(112.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(accent.copy(alpha = 0.12f), size.minDimension * 0.48f, center)
        drawCircle(accent, size.minDimension * 0.39f, center, style = Stroke(5f))
        if (victory) {
            drawLine(
                accent,
                Offset(size.width * 0.28f, size.height * 0.52f),
                Offset(size.width * 0.45f, size.height * 0.68f),
                strokeWidth = 9f,
                cap = StrokeCap.Round
            )
            drawLine(
                accent,
                Offset(size.width * 0.45f, size.height * 0.68f),
                Offset(size.width * 0.75f, size.height * 0.34f),
                strokeWidth = 9f,
                cap = StrokeCap.Round
            )
        } else {
            drawLine(
                accent,
                Offset(size.width * 0.34f, size.height * 0.34f),
                Offset(size.width * 0.66f, size.height * 0.66f),
                strokeWidth = 9f,
                cap = StrokeCap.Round
            )
            drawLine(
                accent,
                Offset(size.width * 0.66f, size.height * 0.34f),
                Offset(size.width * 0.34f, size.height * 0.66f),
                strokeWidth = 9f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun CommandTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    backLabel: String = "BACK"
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text(backLabel, color = SonarCyan) }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = SonarCyan)
            }
            Spacer(Modifier.width(64.dp))
        }
    }
}

@Composable
private fun TacticalChip(label: String, color: Color = SonarCyan) {
    Surface(
        color = color.copy(alpha = 0.11f),
        contentColor = color,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun BoardLegend(missLabel: String, hitLabel: String, selectedLabel: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LegendItem(SonarCyan, missLabel)
        LegendItem(ImpactCoral, hitLabel)
        LegendItem(SignalAmber, selectedLabel)
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
