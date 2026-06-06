package ch.davide.pham.battleshipproject.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import ch.davide.pham.battleshipproject.ui.AppLanguage
import ch.davide.pham.battleshipproject.ui.GameUiState
import ch.davide.pham.battleshipproject.ui.ServerStatus
import ch.davide.pham.battleshipproject.ui.strings
import ch.davide.pham.battleshipproject.ui.theme.DeepNavy
import ch.davide.pham.battleshipproject.ui.theme.Fog
import ch.davide.pham.battleshipproject.ui.theme.ImpactCoral
import ch.davide.pham.battleshipproject.ui.theme.SignalAmber
import ch.davide.pham.battleshipproject.ui.theme.SonarCyan
import ch.davide.pham.battleshipproject.ui.theme.SuccessMint

/*
 * Author: Davide Pham
 * Home and connection screens for the Battleship project.
 * OpenAI Codex assisted with implementation and visual design; see README.md.
 */
@Composable
fun HomeScreen(
    state: GameUiState,
    onEnter: () -> Unit,
    onSettings: () -> Unit,
    onHowToPlay: () -> Unit,
    onAbout: () -> Unit
) {
    val text = state.language.strings
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 22.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text.fleetCommand,
                    color = DeepNavy,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text.battleship,
                    color = DeepNavy,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black
                )
            }
            NavalCompass()
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .align(Alignment.CenterHorizontally),
            color = DeepNavy.copy(alpha = 0.9f),
            contentColor = Fog,
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 14.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text.homeTagline,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Fog.copy(alpha = 0.82f)
                )
                
                Button(
                    onClick = onEnter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SonarCyan,
                        contentColor = DeepNavy
                    )
                ) {
                    Text(text.enterBattle)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MenuButton(text.settings, onSettings, Modifier.weight(1f))
                    MenuButton(text.howToPlay, onHowToPlay, Modifier.weight(1f))
                }
                OutlinedButton(
                    onClick = onAbout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Fog)
                ) {
                    Text(text.about)
                }
            }
        }
    }
}

@Composable
private fun MenuButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = Color.White.copy(alpha = 0.1f),
            contentColor = Fog
        )
    ) {
        Text(label, maxLines = 1)
    }
}

@Composable
fun JoinGameScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onPlayerNameChange: (String) -> Unit,
    onGameKeyChange: (String) -> Unit,
    onToggleMode: (Boolean) -> Unit, // isHost: Boolean
    onGenerateKey: () -> Unit,
    onPing: () -> Unit,
    onContinue: () -> Unit
) {
    val text = state.language.strings
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
    ) {
        SceneTopBar(text.joinTitle, text.backToMenu, onBack)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 540.dp),
                color = DeepNavy.copy(alpha = 0.93f),
                contentColor = Fog,
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 14.dp
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TabRow(
                        selectedTabIndex = if (state.isHostMode) 0 else 1,
                        containerColor = Color.Transparent,
                        contentColor = SonarCyan,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[if (state.isHostMode) 0 else 1]),
                                color = SonarCyan
                            )
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = state.isHostMode,
                            onClick = { onToggleMode(true) },
                            text = { Text("HOST") }
                        )
                        Tab(
                            selected = !state.isHostMode,
                            onClick = { onToggleMode(false) },
                            text = { Text("JOIN") }
                        )
                    }

                    Text(
                        text.joinSubtitle,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    LocalizedServerBadge(state)
                    
                    val textFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = SonarCyan,
                        unfocusedLabelColor = Fog.copy(alpha = 0.7f),
                        focusedBorderColor = SonarCyan,
                        unfocusedBorderColor = Fog.copy(alpha = 0.3f),
                        cursorColor = SonarCyan
                    )

                    OutlinedTextField(
                        value = state.playerName,
                        onValueChange = onPlayerNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(text.playerName) },
                        supportingText = { Text(text.minimumThree) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = textFieldColors
                    )
                    
                    OutlinedTextField(
                        value = state.gameKey,
                        onValueChange = onGameKeyChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = state.isHostMode,
                        label = { Text(text.gameKey) },
                        supportingText = { Text(text.shareKey) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = textFieldColors,
                        trailingIcon = {
                            if (state.isHostMode) {
                                TextButton(onClick = onGenerateKey) {
                                    Text("KEY +", color = SonarCyan)
                                }
                            }
                        }
                    )

                    OutlinedButton(
                        onClick = onPing,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SonarCyan)
                    ) {
                        Text(text.pingServer)
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DeepNavy.copy(alpha = 0.97f),
            shadowElevation = 18.dp
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 14.dp)
                    .height(58.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isHostMode) SonarCyan else SuccessMint,
                    contentColor = DeepNavy
                )
            ) {
                Text(
                    text = if (state.isHostMode) text.createGame else text.joinGame,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onLanguage: (AppLanguage) -> Unit,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit
) {
    val text = state.language.strings
    InfoPage(title = text.settings, backLabel = text.backToMenu, onBack = onBack) {
        Text(text.language, style = MaterialTheme.typography.labelLarge, color = SonarCyan)
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            AppLanguage.entries.forEach { language ->
                val selected = language == state.language
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (selected) SonarCyan else Fog.copy(alpha = 0.16f),
                            RoundedCornerShape(16.dp)
                        )
                        .background(
                            if (selected) SonarCyan.copy(alpha = 0.13f) else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onLanguage(language) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(language.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(language.code, color = if (selected) SonarCyan else Fog.copy(alpha = 0.65f))
                }
            }
        }
        SettingSwitch(text.soundEffects, state.soundEnabled, onToggleSound)
        SettingSwitch(text.backgroundMusic, state.musicEnabled, onToggleMusic)
        Text(
            text.settingsNote,
            style = MaterialTheme.typography.bodySmall,
            color = Fog.copy(alpha = 0.62f)
        )
    }
}

@Composable
fun HowToPlayScreen(state: GameUiState, onBack: () -> Unit) {
    val text = state.language.strings
    InfoPage(title = text.howToPlay, backLabel = text.backToMenu, onBack = onBack) {
        Text(text.howIntro, style = MaterialTheme.typography.titleMedium, color = SonarCyan)
        listOf(
            text.howStepOne,
            text.howStepTwo,
            text.howStepThree,
            text.howStepFour,
            text.howStepFive
        ).forEach { step ->
            Text(step, style = MaterialTheme.typography.bodyLarge, color = Fog.copy(alpha = 0.85f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            InfoLegend(ImpactCoral, text.hit)
            InfoLegend(SonarCyan, text.miss)
            InfoLegend(SignalAmber, text.selectedVessel)
        }
    }
}

@Composable
fun AboutScreen(state: GameUiState, onBack: () -> Unit) {
    val text = state.language.strings
    InfoPage(title = text.about, backLabel = text.backToMenu, onBack = onBack) {
        Text(text.battleship, style = MaterialTheme.typography.headlineMedium, color = SonarCyan)
        Text(text.aboutDescription, style = MaterialTheme.typography.bodyLarge)
        Text(text.technologies, style = MaterialTheme.typography.bodyLarge, color = Fog.copy(alpha = 0.78f))
        Surface(
            color = SonarCyan.copy(alpha = 0.12f),
            contentColor = SonarCyan,
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                "SERVER  brad-home.ch:50003",
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
        Text(
            "FHNW Mobile Applications with Android\nDavide Pham",
            style = MaterialTheme.typography.bodyMedium,
            color = Fog.copy(alpha = 0.68f)
        )
    }
}

@Composable
private fun InfoPage(
    title: String,
    backLabel: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        SceneTopBar(title, backLabel, onBack)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 650.dp),
                color = DeepNavy.copy(alpha = 0.93f),
                contentColor = Fog,
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 14.dp
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
private fun SceneTopBar(title: String, backLabel: String, onBack: () -> Unit) {
    Surface(
        color = DeepNavy.copy(alpha = 0.9f),
        contentColor = Fog
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text(backLabel, color = SonarCyan) }
            Text(
                title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.size(64.dp))
        }
    }
}

@Composable
private fun LocalizedServerBadge(state: GameUiState) {
    val text = state.language.strings
    val color = when (state.serverStatus) {
        ServerStatus.CHECKING -> SignalAmber
        ServerStatus.ONLINE -> SuccessMint
        ServerStatus.OFFLINE -> ImpactCoral
    }
    val label = when (state.serverStatus) {
        ServerStatus.CHECKING -> text.checkingServer
        ServerStatus.ONLINE -> text.serverOnline
        ServerStatus.OFFLINE -> text.serverOffline
    }
    Surface(
        color = color.copy(alpha = 0.14f),
        contentColor = color,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.size(8.dp).background(color, CircleShape))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Switch(checked = checked, onCheckedChange = { onToggle() })
    }
}

@Composable
private fun InfoLegend(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun NavalCompass() {
    Canvas(Modifier.size(72.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(DeepNavy.copy(alpha = 0.88f), size.minDimension * 0.48f, center)
        drawCircle(SonarCyan, size.minDimension * 0.37f, center, style = Stroke(3f))
        drawLine(
            SignalAmber,
            Offset(center.x, size.height * 0.2f),
            Offset(center.x, size.height * 0.5f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
        drawLine(
            SonarCyan,
            Offset(size.width * 0.25f, size.height * 0.66f),
            Offset(size.width * 0.75f, size.height * 0.66f),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
    }
}
