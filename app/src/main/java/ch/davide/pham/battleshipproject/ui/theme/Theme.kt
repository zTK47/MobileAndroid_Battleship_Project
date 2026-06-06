package ch.davide.pham.battleshipproject.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/*
 * Author: Davide Pham (individual FHNW student project).
 * OpenAI Codex assisted with the Material 3 color and appearance system.
 */
private val DarkColorScheme = darkColorScheme(
    primary = SonarCyan,
    onPrimary = Ink,
    primaryContainer = PanelBlue,
    onPrimaryContainer = IceBlue,
    secondary = SignalAmber,
    onSecondary = Ink,
    tertiary = SuccessMint,
    background = Abyss,
    onBackground = Fog,
    surface = DeepNavy,
    onSurface = Fog,
    surfaceVariant = CommandBlue,
    onSurfaceVariant = IceBlue,
    error = ImpactCoral,
    outline = Steel
)

private val LightColorScheme = lightColorScheme(
    primary = Ocean,
    onPrimary = Color.White,
    primaryContainer = LightPanel,
    onPrimaryContainer = Ink,
    secondary = Color(0xFFA76500),
    onSecondary = Color.White,
    tertiary = Color(0xFF087A50),
    background = LightSurface,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = LightPanel,
    onSurfaceVariant = CommandBlue,
    error = Color(0xFFB3261E),
    outline = Color(0xFF55717E)
)

@Composable
fun MobileAndroidApplicationBattleshipProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = FleetShapes,
        content = content
    )
}
