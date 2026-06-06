package ch.davide.pham.battleshipproject.model

import kotlin.random.Random

/*
 * Author: Davide Pham (individual FHNW student project).
 * Pure game rules are kept independent from Android so they can be unit tested.
 * OpenAI Codex assisted with implementation; see README.md for attribution.
 */

const val BOARD_SIZE = 10

enum class ShipType(
    val apiName: String,
    val displayName: String,
    val size: Int,
    val tacticalCode: String
) {
    CARRIER("Carrier", "Carrier", 5, "CV"),
    BATTLESHIP("Battleship", "Battleship", 4, "BB"),
    DESTROYER("Destroyer", "Destroyer", 3, "DD"),
    SUBMARINE("Submarine", "Submarine", 3, "SS"),
    PATROL_BOAT("PatrolBoat", "Patrol Boat", 2, "PB");

    companion object {
        fun fromApiName(value: String): ShipType? =
            entries.firstOrNull { it.apiName.equals(value, ignoreCase = true) }
    }
}

enum class Orientation(val apiValue: String) {
    HORIZONTAL("horizontal"),
    VERTICAL("vertical");

    fun toggled(): Orientation =
        if (this == HORIZONTAL) VERTICAL else HORIZONTAL
}

data class GridCoordinate(val x: Int, val y: Int) {
    val isOnBoard: Boolean
        get() = x in 0 until BOARD_SIZE && y in 0 until BOARD_SIZE

    val label: String
        get() = "${('A'.code + x).toChar()}${y + 1}"
}

data class PlacedShip(
    val type: ShipType,
    val x: Int,
    val y: Int,
    val orientation: Orientation
) {
    fun cells(): List<GridCoordinate> =
        List(type.size) { index ->
            GridCoordinate(
                x = if (orientation == Orientation.HORIZONTAL) x + index else x,
                y = if (orientation == Orientation.VERTICAL) y + index else y
            )
        }

    fun toNetworkModel(): ShipPosition =
        ShipPosition(
            ship = type.apiName,
            x = x,
            y = y,
            orientation = orientation.apiValue
        )
}

enum class ShotResult {
    HIT,
    MISS
}

object FleetRules {
    private val requiredTypes = ShipType.entries.toSet()

    fun isCompleteAndValid(fleet: List<PlacedShip>): Boolean {
        if (fleet.map { it.type }.toSet() != requiredTypes || fleet.size != requiredTypes.size) {
            return false
        }
        return fleet.all { ship ->
            canPlace(fleet.filterNot { it.type == ship.type }, ship)
        }
    }

    fun canPlace(existingFleet: List<PlacedShip>, candidate: PlacedShip): Boolean {
        val candidateCells = candidate.cells()
        if (candidateCells.any { !it.isOnBoard }) return false

        val occupied = existingFleet
            .filterNot { it.type == candidate.type }
            .flatMap { it.cells() }
            .toSet()
        return candidateCells.none { it in occupied }
    }

    fun placeOrMove(
        fleet: List<PlacedShip>,
        type: ShipType,
        coordinate: GridCoordinate,
        orientation: Orientation
    ): List<PlacedShip>? {
        val candidate = PlacedShip(type, coordinate.x, coordinate.y, orientation)
        if (!canPlace(fleet, candidate)) return null

        return fleet
            .filterNot { it.type == type }
            .plus(candidate)
            .sortedBy { it.type.ordinal }
    }

    fun shipAt(fleet: List<PlacedShip>, coordinate: GridCoordinate): PlacedShip? =
        fleet.firstOrNull { coordinate in it.cells() }

    fun randomFleet(random: Random = Random.Default): List<PlacedShip> {
        val result = mutableListOf<PlacedShip>()
        ShipType.entries.forEach { type ->
            var placed = false
            while (!placed) {
                val orientation = if (random.nextBoolean()) {
                    Orientation.HORIZONTAL
                } else {
                    Orientation.VERTICAL
                }
                val maxX = if (orientation == Orientation.HORIZONTAL) {
                    BOARD_SIZE - type.size
                } else {
                    BOARD_SIZE - 1
                }
                val maxY = if (orientation == Orientation.VERTICAL) {
                    BOARD_SIZE - type.size
                } else {
                    BOARD_SIZE - 1
                }
                val candidate = PlacedShip(
                    type = type,
                    x = random.nextInt(maxX + 1),
                    y = random.nextInt(maxY + 1),
                    orientation = orientation
                )
                if (canPlace(result, candidate)) {
                    result += candidate
                    placed = true
                }
            }
        }
        return result
    }
}

fun PlacedShip.isSunk(incomingShots: Map<GridCoordinate, ShotResult>): Boolean =
    cells().all { incomingShots[it] == ShotResult.HIT }

fun List<PlacedShip>.allSunk(incomingShots: Map<GridCoordinate, ShotResult>): Boolean =
    isNotEmpty() && all { it.isSunk(incomingShots) }

fun List<PlacedShip>.containsShipAt(coordinate: GridCoordinate): Boolean =
    any { coordinate in it.cells() }
