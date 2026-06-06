package ch.davide.pham.battleshipproject.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import ch.davide.pham.battleshipproject.model.BOARD_SIZE
import ch.davide.pham.battleshipproject.model.GridCoordinate
import ch.davide.pham.battleshipproject.model.Orientation
import ch.davide.pham.battleshipproject.model.PlacedShip
import ch.davide.pham.battleshipproject.model.ShipType
import ch.davide.pham.battleshipproject.model.ShotResult
import ch.davide.pham.battleshipproject.model.isSunk
import ch.davide.pham.battleshipproject.ui.theme.BattleshipColor
import ch.davide.pham.battleshipproject.ui.theme.CarrierColor
import ch.davide.pham.battleshipproject.ui.theme.DeepNavy
import ch.davide.pham.battleshipproject.ui.theme.DestroyerColor
import ch.davide.pham.battleshipproject.ui.theme.ImpactCoral
import ch.davide.pham.battleshipproject.ui.theme.PatrolColor
import ch.davide.pham.battleshipproject.ui.theme.SignalAmber
import ch.davide.pham.battleshipproject.ui.theme.SonarCyan
import ch.davide.pham.battleshipproject.ui.theme.SubmarineColor
import kotlin.math.floor
import kotlin.math.min

/*
 * Author: Davide Pham (individual FHNW student project).
 * OpenAI Codex assisted with the custom Canvas implementation.
 */
@Composable
fun BattleBoard(
    fleet: List<PlacedShip>,
    shots: Map<GridCoordinate, ShotResult>,
    showFleet: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    selectedShip: ShipType? = null,
    highlightedCoordinate: GridCoordinate? = null,
    onCellTap: (GridCoordinate) -> Unit = {}
) {
    val transition = rememberInfiniteTransition(label = "target-pulse")
    val pulse = transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "target-pulse-alpha"
    ).value

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures { offset ->
                    val geometry = BoardGeometry(size.width.toFloat(), size.height.toFloat())
                    geometry.coordinateAt(offset)?.let(onCellTap)
                }
            }
    ) {
        val geometry = BoardGeometry(size.width, size.height)
        drawBoardBase(geometry)
        if (showFleet) {
            fleet.forEach { ship ->
                drawShip(
                    geometry = geometry,
                    ship = ship,
                    selected = ship.type == selectedShip,
                    sunk = ship.isSunk(shots)
                )
            }
        }
        shots.forEach { (coordinate, result) ->
            drawShot(geometry, coordinate, result)
        }
        highlightedCoordinate?.let { coordinate ->
            val topLeft = geometry.cellTopLeft(coordinate)
            drawRoundRect(
                color = SignalAmber.copy(alpha = pulse * 0.55f),
                topLeft = topLeft + Offset(geometry.cellSize * 0.08f, geometry.cellSize * 0.08f),
                size = Size(geometry.cellSize * 0.84f, geometry.cellSize * 0.84f),
                cornerRadius = CornerRadius(geometry.cellSize * 0.18f),
                style = Stroke(width = geometry.cellSize * 0.08f)
            )
        }
    }
}

private data class BoardGeometry(val width: Float, val height: Float) {
    val boardSize = min(width, height)
    val labelInset = boardSize * 0.085f
    val gridSize = boardSize - labelInset
    val cellSize = gridSize / BOARD_SIZE
    val origin = Offset(labelInset, labelInset)

    fun cellTopLeft(coordinate: GridCoordinate): Offset =
        Offset(
            origin.x + coordinate.x * cellSize,
            origin.y + coordinate.y * cellSize
        )

    fun cellCenter(coordinate: GridCoordinate): Offset =
        cellTopLeft(coordinate) + Offset(cellSize / 2f, cellSize / 2f)

    fun coordinateAt(offset: Offset): GridCoordinate? {
        if (
            offset.x < origin.x ||
            offset.y < origin.y ||
            offset.x >= origin.x + gridSize ||
            offset.y >= origin.y + gridSize
        ) {
            return null
        }
        val x = floor((offset.x - origin.x) / cellSize).toInt()
        val y = floor((offset.y - origin.y) / cellSize).toInt()
        return GridCoordinate(x, y)
    }
}

private fun DrawScope.drawBoardBase(geometry: BoardGeometry) {
    drawRoundRect(
        color = DeepNavy.copy(alpha = 0.9f),
        topLeft = Offset.Zero,
        size = Size(geometry.boardSize, geometry.boardSize),
        cornerRadius = CornerRadius(geometry.boardSize * 0.035f)
    )

    for (y in 0 until BOARD_SIZE) {
        for (x in 0 until BOARD_SIZE) {
            val coordinate = GridCoordinate(x, y)
            val topLeft = geometry.cellTopLeft(coordinate)
            val color = if ((x + y) % 2 == 0) {
                Color(0xFF0E5A7A)
            } else {
                Color(0xFF0B4E6D)
            }
            drawRect(
                color = color,
                topLeft = topLeft,
                size = Size(geometry.cellSize, geometry.cellSize)
            )
            drawLine(
                color = SonarCyan.copy(alpha = 0.16f),
                start = Offset(topLeft.x, topLeft.y + geometry.cellSize * 0.72f),
                end = Offset(topLeft.x + geometry.cellSize, topLeft.y + geometry.cellSize * 0.72f),
                strokeWidth = 1f
            )
        }
    }

    for (line in 0..BOARD_SIZE) {
        val position = line * geometry.cellSize
        drawLine(
            color = SonarCyan.copy(alpha = 0.34f),
            start = Offset(geometry.origin.x + position, geometry.origin.y),
            end = Offset(geometry.origin.x + position, geometry.origin.y + geometry.gridSize),
            strokeWidth = 1.2f
        )
        drawLine(
            color = SonarCyan.copy(alpha = 0.34f),
            start = Offset(geometry.origin.x, geometry.origin.y + position),
            end = Offset(geometry.origin.x + geometry.gridSize, geometry.origin.y + position),
            strokeWidth = 1.2f
        )
    }

    val labelPaint = Paint().apply {
        color = SonarCyan.toArgb()
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textSize = geometry.labelInset * 0.34f
    }
    drawContext.canvas.nativeCanvas.apply {
        for (index in 0 until BOARD_SIZE) {
            val center = geometry.origin.x + (index + 0.5f) * geometry.cellSize
            drawText(
                ('A'.code + index).toChar().toString(),
                center,
                geometry.labelInset * 0.62f,
                labelPaint
            )
            val verticalCenter = geometry.origin.y + (index + 0.5f) * geometry.cellSize
            drawText(
                (index + 1).toString(),
                geometry.labelInset * 0.48f,
                verticalCenter - (labelPaint.ascent() + labelPaint.descent()) / 2f,
                labelPaint
            )
        }
    }
}

private fun DrawScope.drawShip(
    geometry: BoardGeometry,
    ship: PlacedShip,
    selected: Boolean,
    sunk: Boolean
) {
    val color = if (sunk) ImpactCoral.copy(alpha = 0.82f) else shipColor(ship.type)
    val firstCell = geometry.cellTopLeft(GridCoordinate(ship.x, ship.y))
    val inset = geometry.cellSize * 0.11f
    val bodyLength = geometry.cellSize * ship.type.size - inset * 2f
    val bodyThickness = geometry.cellSize - inset * 2f
    val bodyPath = Path()

    if (ship.orientation == Orientation.HORIZONTAL) {
        val left = firstCell.x + inset
        val top = firstCell.y + inset
        val right = left + bodyLength
        val bottom = top + bodyThickness
        val middle = top + bodyThickness / 2f
        bodyPath.apply {
            moveTo(left, middle)
            lineTo(left + geometry.cellSize * 0.34f, top)
            lineTo(right - geometry.cellSize * 0.16f, top)
            lineTo(right, top + bodyThickness * 0.22f)
            lineTo(right, bottom - bodyThickness * 0.22f)
            lineTo(right - geometry.cellSize * 0.16f, bottom)
            lineTo(left + geometry.cellSize * 0.34f, bottom)
            close()
        }
    } else {
        val left = firstCell.x + inset
        val top = firstCell.y + inset
        val right = left + bodyThickness
        val bottom = top + bodyLength
        val middle = left + bodyThickness / 2f
        bodyPath.apply {
            moveTo(middle, top)
            lineTo(left, top + geometry.cellSize * 0.34f)
            lineTo(left, bottom - geometry.cellSize * 0.16f)
            lineTo(left + bodyThickness * 0.22f, bottom)
            lineTo(right - bodyThickness * 0.22f, bottom)
            lineTo(right, bottom - geometry.cellSize * 0.16f)
            lineTo(right, top + geometry.cellSize * 0.34f)
            close()
        }
    }

    drawPath(
        path = bodyPath,
        color = color.copy(alpha = if (selected) 0.98f else 0.88f)
    )
    drawPath(
        path = bodyPath,
        color = if (selected) SignalAmber else Color.White.copy(alpha = 0.34f),
        style = Stroke(width = geometry.cellSize * if (selected) 0.07f else 0.025f)
    )

    val cells = ship.cells()
    val firstCenter = geometry.cellCenter(cells.first())
    val lastCenter = geometry.cellCenter(cells.last())
    drawLine(
        color = DeepNavy.copy(alpha = 0.48f),
        start = firstCenter,
        end = lastCenter,
        strokeWidth = geometry.cellSize * 0.08f,
        cap = StrokeCap.Round
    )

    cells.forEachIndexed { index, coordinate ->
        val center = geometry.cellCenter(coordinate)
        drawCircle(
            color = DeepNavy.copy(alpha = 0.72f),
            radius = geometry.cellSize * 0.065f,
            center = center
        )
        drawCircle(
            color = SignalAmber.copy(alpha = 0.9f),
            radius = geometry.cellSize * 0.025f,
            center = center
        )
        if (index == 1 || (ship.type.size >= 4 && index == ship.type.size - 2)) {
            drawCircle(
                color = Color(0xFFB7D4DE),
                radius = geometry.cellSize * 0.12f,
                center = center
            )
            val gunOffset = if (ship.orientation == Orientation.HORIZONTAL) {
                Offset(geometry.cellSize * 0.22f, -geometry.cellSize * 0.13f)
            } else {
                Offset(geometry.cellSize * 0.13f, geometry.cellSize * 0.22f)
            }
            drawLine(
                color = DeepNavy,
                start = center,
                end = center + gunOffset,
                strokeWidth = geometry.cellSize * 0.065f,
                cap = StrokeCap.Round
            )
        }
    }

    val bridgeCoordinate = cells[cells.size / 2]
    val bridgeCenter = geometry.cellCenter(bridgeCoordinate)
    drawRoundRect(
        color = Color(0xFFD4E8ED).copy(alpha = if (sunk) 0.55f else 0.88f),
        topLeft = bridgeCenter - Offset(geometry.cellSize * 0.15f, geometry.cellSize * 0.13f),
        size = Size(geometry.cellSize * 0.3f, geometry.cellSize * 0.26f),
        cornerRadius = CornerRadius(geometry.cellSize * 0.06f)
    )
}

private fun DrawScope.drawShot(
    geometry: BoardGeometry,
    coordinate: GridCoordinate,
    result: ShotResult
) {
    val center = geometry.cellCenter(coordinate)
    when (result) {
        ShotResult.HIT -> {
            drawCircle(
                color = ImpactCoral.copy(alpha = 0.24f),
                radius = geometry.cellSize * 0.42f,
                center = center
            )
            drawCircle(
                color = ImpactCoral,
                radius = geometry.cellSize * 0.23f,
                center = center
            )
            val arm = geometry.cellSize * 0.2f
            drawLine(
                color = Color.White,
                start = center - Offset(arm, arm),
                end = center + Offset(arm, arm),
                strokeWidth = geometry.cellSize * 0.07f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White,
                start = center + Offset(arm, -arm),
                end = center + Offset(-arm, arm),
                strokeWidth = geometry.cellSize * 0.07f,
                cap = StrokeCap.Round
            )
        }

        ShotResult.MISS -> {
            drawCircle(
                color = SonarCyan.copy(alpha = 0.9f),
                radius = geometry.cellSize * 0.17f,
                center = center,
                style = Stroke(width = geometry.cellSize * 0.06f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = geometry.cellSize * 0.045f,
                center = center
            )
        }
    }
}

fun shipColor(type: ShipType): Color =
    when (type) {
        ShipType.CARRIER -> CarrierColor
        ShipType.BATTLESHIP -> BattleshipColor
        ShipType.DESTROYER -> DestroyerColor
        ShipType.SUBMARINE -> SubmarineColor
        ShipType.PATROL_BOAT -> PatrolColor
    }
