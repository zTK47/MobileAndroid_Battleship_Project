package ch.davide.pham.battleshipproject.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import ch.davide.pham.battleshipproject.ui.theme.DeepNavy
import ch.davide.pham.battleshipproject.ui.theme.Fog
import ch.davide.pham.battleshipproject.ui.theme.ImpactCoral
import ch.davide.pham.battleshipproject.ui.theme.SignalAmber
import ch.davide.pham.battleshipproject.ui.theme.SonarCyan
import ch.davide.pham.battleshipproject.ui.theme.SuccessMint
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class SeaSceneMood {
    DAY,
    VICTORY,
    DEFEAT
}

/*
 * Author: Davide Pham (individual FHNW student project).
 * Original full-screen naval artwork drawn with Jetpack Compose Canvas.
 * OpenAI Codex assisted with implementation and visual design; see README.md.
 */
@Composable
fun SeaSceneBackground(
    modifier: Modifier = Modifier,
    mood: SeaSceneMood = SeaSceneMood.DAY
) {
    val transition = rememberInfiniteTransition(label = "full-sea-scene")
    val phase = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sea-phase"
    ).value
    val pulse = transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scene-pulse"
    ).value

    Canvas(modifier.fillMaxSize()) {
        val horizon = size.height * 0.57f
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF48C9E6),
                    Color(0xFF8CE5F0),
                    Color(0xFFD6F6F5)
                ),
                endY = horizon
            )
        )

        drawCircle(
            color = SignalAmber.copy(alpha = 0.2f),
            radius = size.minDimension * 0.2f,
            center = Offset(size.width * 0.78f, size.height * 0.16f)
        )
        drawCircle(
            color = SignalAmber,
            radius = size.minDimension * 0.07f,
            center = Offset(size.width * 0.78f, size.height * 0.16f)
        )

        drawSceneCloud(Offset(size.width * 0.16f, size.height * 0.19f), size.width * 0.28f)
        drawSceneCloud(Offset(size.width * 0.52f, size.height * 0.29f), size.width * 0.21f)
        drawSceneCloud(Offset(size.width * 0.9f, size.height * 0.35f), size.width * 0.25f)

        drawIsland(horizon)
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF1AA6C3), Color(0xFF087497), Color(0xFF043B5A)),
                startY = horizon,
                endY = size.height
            ),
            topLeft = Offset(0f, horizon),
            size = Size(size.width, size.height - horizon)
        )
        drawSceneWaves(horizon, phase)

        val shipY = when (mood) {
            SeaSceneMood.DEFEAT -> horizon - size.height * 0.005f
            else -> horizon - size.height * 0.055f
        }
        drawLargeShip(
            origin = Offset(size.width * 0.06f, shipY + sin(phase * 2f * PI).toFloat() * 3.dp.toPx()),
            width = size.width * 0.63f,
            sinking = mood == SeaSceneMood.DEFEAT
        )
        drawSceneLighthouse(horizon, pulse)

        when (mood) {
            SeaSceneMood.VICTORY -> drawFireworks(phase)
            SeaSceneMood.DEFEAT -> drawExplosion(
                center = Offset(size.width * 0.48f, horizon - size.height * 0.035f),
                pulse = pulse
            )
            SeaSceneMood.DAY -> drawSonar(phase)
        }

        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color.Transparent, DeepNavy.copy(alpha = 0.32f)),
                startY = size.height * 0.55f,
                endY = size.height
            )
        )
    }
}

private fun DrawScope.drawSceneCloud(center: Offset, width: Float) {
    val color = Fog.copy(alpha = 0.8f)
    drawOval(
        color = color,
        topLeft = Offset(center.x - width * 0.5f, center.y),
        size = Size(width, width * 0.26f)
    )
    drawCircle(color, width * 0.19f, Offset(center.x - width * 0.22f, center.y))
    drawCircle(color, width * 0.24f, Offset(center.x + width * 0.02f, center.y - width * 0.04f))
    drawCircle(color, width * 0.17f, Offset(center.x + width * 0.3f, center.y + width * 0.02f))
}

private fun DrawScope.drawIsland(horizon: Float) {
    val island = Path().apply {
        moveTo(size.width * 0.68f, horizon)
        lineTo(size.width * 0.76f, horizon - size.height * 0.045f)
        lineTo(size.width * 0.84f, horizon - size.height * 0.025f)
        lineTo(size.width * 0.92f, horizon - size.height * 0.075f)
        lineTo(size.width, horizon - size.height * 0.02f)
        lineTo(size.width, horizon)
        close()
    }
    drawPath(island, Color(0xFF4E8E55))
}

private fun DrawScope.drawSceneWaves(horizon: Float, phase: Float) {
    repeat(14) { line ->
        val path = Path()
        val baseY = horizon + (size.height - horizon) * (line + 0.5f) / 14f
        repeat(34) { segment ->
            val x = size.width * segment / 33f
            val y = baseY + sin(segment * 0.75f + phase * 2f * PI + line).toFloat() *
                (2.dp.toPx() + line * 0.12.dp.toPx())
            if (segment == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path,
            Fog.copy(alpha = 0.1f + line * 0.008f),
            style = Stroke(width = 1.3.dp.toPx())
        )
    }
}

private fun DrawScope.drawLargeShip(origin: Offset, width: Float, sinking: Boolean) {
    val height = width * 0.27f
    val rotationOffset = if (sinking) height * 0.2f else 0f
    val hull = Path().apply {
        moveTo(origin.x, origin.y + height * 0.52f)
        lineTo(origin.x + width * 0.9f, origin.y + height * 0.52f + rotationOffset)
        lineTo(origin.x + width, origin.y + height * 0.35f + rotationOffset)
        lineTo(origin.x + width * 0.84f, origin.y + height * 0.82f + rotationOffset)
        lineTo(origin.x + width * 0.16f, origin.y + height * 0.82f)
        close()
    }
    drawPath(hull, DeepNavy)
    drawPath(hull, SonarCyan.copy(alpha = 0.65f), style = Stroke(1.2.dp.toPx()))

    drawRoundRect(
        color = Color(0xFF41697B),
        topLeft = Offset(origin.x + width * 0.26f, origin.y + height * 0.29f),
        size = Size(width * 0.43f, height * 0.25f)
    )
    drawRect(
        color = Color(0xFF668A99),
        topLeft = Offset(origin.x + width * 0.43f, origin.y + height * 0.06f),
        size = Size(width * 0.16f, height * 0.24f)
    )
    drawRect(
        color = DeepNavy,
        topLeft = Offset(origin.x + width * 0.49f, origin.y - height * 0.08f),
        size = Size(width * 0.014f, height * 0.16f)
    )
    listOf(0.19f, 0.76f).forEach { position ->
        val center = Offset(origin.x + width * position, origin.y + height * 0.39f)
        drawCircle(Color(0xFF7895A2), height * 0.09f, center)
        drawLine(
            DeepNavy,
            center,
            center + Offset(width * 0.11f, -height * 0.1f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
    repeat(5) { index ->
        drawCircle(
            SignalAmber,
            1.8.dp.toPx(),
            Offset(origin.x + width * (0.37f + index * 0.055f), origin.y + height * 0.42f)
        )
    }
}

private fun DrawScope.drawSceneLighthouse(horizon: Float, pulse: Float) {
    val x = size.width * 0.89f
    val base = horizon - size.height * 0.02f
    val towerHeight = size.height * 0.18f
    val towerWidth = size.width * 0.055f
    val tower = Path().apply {
        moveTo(x - towerWidth * 0.7f, base)
        lineTo(x - towerWidth * 0.35f, base - towerHeight)
        lineTo(x + towerWidth * 0.35f, base - towerHeight)
        lineTo(x + towerWidth * 0.7f, base)
        close()
    }
    drawPath(tower, Fog)
    repeat(3) { band ->
        drawRect(
            ImpactCoral,
            Offset(x - towerWidth * 0.48f, base - towerHeight * (0.22f + band * 0.27f)),
            Size(towerWidth * 0.96f, towerHeight * 0.11f)
        )
    }
    drawRect(
        DeepNavy,
        Offset(x - towerWidth * 0.55f, base - towerHeight - 5.dp.toPx()),
        Size(towerWidth * 1.1f, 7.dp.toPx())
    )
    drawCircle(
        SignalAmber.copy(alpha = pulse),
        towerWidth * (0.55f + pulse * 0.25f),
        Offset(x, base - towerHeight)
    )
}

private fun DrawScope.drawSonar(phase: Float) {
    val center = Offset(size.width * 0.14f, size.height * 0.75f)
    repeat(3) { ring ->
        drawCircle(
            SonarCyan.copy(alpha = 0.12f - ring * 0.02f),
            size.minDimension * (0.05f + ring * 0.04f),
            center,
            style = Stroke(1.2.dp.toPx())
        )
    }
    val angle = phase * 2f * PI.toFloat()
    drawLine(
        SonarCyan.copy(alpha = 0.55f),
        center,
        center + Offset(
            cos(angle.toDouble()).toFloat() * size.minDimension * 0.13f,
            sin(angle.toDouble()).toFloat() * size.minDimension * 0.13f
        ),
        strokeWidth = 1.5.dp.toPx()
    )
}

private fun DrawScope.drawFireworks(phase: Float) {
    val centers = listOf(
        Offset(size.width * 0.22f, size.height * 0.14f),
        Offset(size.width * 0.53f, size.height * 0.21f),
        Offset(size.width * 0.78f, size.height * 0.1f)
    )
    val colors = listOf(SignalAmber, ImpactCoral, SuccessMint)
    centers.forEachIndexed { index, center ->
        repeat(14) { ray ->
            val angle = (ray / 14f) * 2f * PI.toFloat() + phase * 0.7f
            val inner = size.minDimension * (0.035f + index * 0.008f)
            val outer = size.minDimension * (0.09f + index * 0.012f)
            drawLine(
                colors[index],
                center + Offset(
                    cos(angle.toDouble()).toFloat() * inner,
                    sin(angle.toDouble()).toFloat() * inner
                ),
                center + Offset(
                    cos(angle.toDouble()).toFloat() * outer,
                    sin(angle.toDouble()).toFloat() * outer
                ),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawExplosion(center: Offset, pulse: Float) {
    drawCircle(
        ImpactCoral.copy(alpha = 0.35f),
        size.minDimension * (0.1f + pulse * 0.04f),
        center
    )
    drawCircle(
        SignalAmber,
        size.minDimension * (0.055f + pulse * 0.025f),
        center
    )
    repeat(12) { ray ->
        val angle = ray / 12f * 2f * PI.toFloat()
        drawLine(
            if (ray % 2 == 0) SignalAmber else ImpactCoral,
            center + Offset(
                cos(angle.toDouble()).toFloat() * size.minDimension * 0.05f,
                sin(angle.toDouble()).toFloat() * size.minDimension * 0.05f
            ),
            center + Offset(
                cos(angle.toDouble()).toFloat() * size.minDimension * 0.13f,
                sin(angle.toDouble()).toFloat() * size.minDimension * 0.13f
            ),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
