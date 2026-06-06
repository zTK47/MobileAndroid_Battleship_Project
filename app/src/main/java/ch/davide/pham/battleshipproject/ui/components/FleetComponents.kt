package ch.davide.pham.battleshipproject.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.davide.pham.battleshipproject.model.PlacedShip
import ch.davide.pham.battleshipproject.model.ShipType
import ch.davide.pham.battleshipproject.model.ShotResult
import ch.davide.pham.battleshipproject.model.isSunk
import ch.davide.pham.battleshipproject.ui.ServerStatus
import ch.davide.pham.battleshipproject.ui.theme.DeepNavy
import ch.davide.pham.battleshipproject.ui.theme.ImpactCoral
import ch.davide.pham.battleshipproject.ui.theme.SignalAmber
import ch.davide.pham.battleshipproject.ui.theme.SonarCyan
import ch.davide.pham.battleshipproject.ui.theme.SuccessMint

/*
 * Author: Davide Pham (individual FHNW student project).
 * OpenAI Codex assisted with implementation; see README.md for attribution.
 */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.91f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        content()
    }
}

@Composable
fun ServerBadge(status: ServerStatus, modifier: Modifier = Modifier) {
    val color = when (status) {
        ServerStatus.CHECKING -> SignalAmber
        ServerStatus.ONLINE -> SuccessMint
        ServerStatus.OFFLINE -> ImpactCoral
    }
    val label = when (status) {
        ServerStatus.CHECKING -> "CHECKING SERVER"
        ServerStatus.ONLINE -> "SERVER ONLINE"
        ServerStatus.OFFLINE -> "SERVER OFFLINE"
    }
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.14f),
        contentColor = color,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun ShipSelector(
    ship: PlacedShip,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = shipColor(ship.type)
    Column(
        modifier = modifier
            .width(126.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(
                if (selected) color.copy(alpha = 0.17f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = ship.type.tacticalCode,
            color = color,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = ship.type.displayName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(ship.type.size) {
                Box(
                    Modifier
                        .height(5.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun FleetStatusList(
    title: String,
    fleet: List<PlacedShip>,
    incomingShots: Map<ch.davide.pham.battleshipproject.model.GridCoordinate, ShotResult>,
    modifier: Modifier = Modifier,
    enemySunk: Set<ShipType>? = null
) {
    GlassPanel(modifier) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = SonarCyan)
            fleet.sortedBy { it.type.ordinal }.forEach { ship ->
                val sunk = enemySunk?.contains(ship.type) ?: ship.isSunk(incomingShots)
                ShipStatusRow(ship.type, sunk)
            }
        }
    }
}

@Composable
private fun ShipStatusRow(type: ShipType, sunk: Boolean) {
    val color = if (sunk) ImpactCoral else shipColor(type)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = type.displayName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = if (sunk) "SUNK" else "ACTIVE",
            color = color,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun RadarLoader(modifier: Modifier = Modifier, compact: Boolean = false) {
    val transition = rememberInfiniteTransition(label = "radar-loader")
    val angle = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing)
        ),
        label = "radar-angle"
    ).value
    val size = if (compact) 34.dp else 150.dp

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = this.size.minDimension * 0.45f
        repeat(3) {
            drawCircle(
                color = SonarCyan.copy(alpha = 0.22f),
                radius = radius * (it + 1) / 3f,
                center = center,
                style = Stroke(width = if (compact) 1.5f else 2.5f)
            )
        }
        val radians = Math.toRadians(angle.toDouble())
        drawLine(
            color = SonarCyan,
            start = center,
            end = Offset(
                center.x + kotlin.math.cos(radians).toFloat() * radius,
                center.y + kotlin.math.sin(radians).toFloat() * radius
            ),
            strokeWidth = if (compact) 2f else 4f
        )
        drawCircle(
            color = SonarCyan,
            radius = if (compact) 2.5f else 6f,
            center = center
        )
    }
}

@Composable
fun StatTile(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.25f), MaterialTheme.shapes.medium)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = accent)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun SectionLabel(
    eyebrow: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            eyebrow,
            style = MaterialTheme.typography.labelMedium,
            color = SonarCyan
        )
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
        )
    }
}
