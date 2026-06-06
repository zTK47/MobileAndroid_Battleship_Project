package ch.davide.pham.battleshipproject

import ch.davide.pham.battleshipproject.model.FleetRules
import ch.davide.pham.battleshipproject.model.GridCoordinate
import ch.davide.pham.battleshipproject.model.Orientation
import ch.davide.pham.battleshipproject.model.PlacedShip
import ch.davide.pham.battleshipproject.model.ShipType
import ch.davide.pham.battleshipproject.model.ShotResult
import ch.davide.pham.battleshipproject.model.allSunk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameDomainTest {
    @Test
    fun fleetSpecificationMatchesCourseProtocol() {
        assertEquals(
            listOf("Carrier", "Battleship", "Destroyer", "Submarine", "PatrolBoat"),
            ShipType.entries.map { it.apiName }
        )
        assertEquals(listOf(5, 4, 3, 3, 2), ShipType.entries.map { it.size })
        assertEquals("horizontal", Orientation.HORIZONTAL.apiValue)
        assertEquals("vertical", Orientation.VERTICAL.apiValue)
    }

    @Test
    fun coordinateLabelsConvertServerIndexesToUserLabels() {
        assertEquals("A1", GridCoordinate(0, 0).label)
        assertEquals("J10", GridCoordinate(9, 9).label)
        assertFalse(GridCoordinate(-1, 0).isOnBoard)
        assertFalse(GridCoordinate(10, 9).isOnBoard)
    }

    @Test
    fun randomFleetAlwaysProducesValidFormation() {
        repeat(100) { seed ->
            val fleet = FleetRules.randomFleet(Random(seed))
            assertTrue(FleetRules.isCompleteAndValid(fleet))
        }
    }

    @Test
    fun overlappingShipPositionIsRejected() {
        val carrier = PlacedShip(
            ShipType.CARRIER,
            x = 0,
            y = 0,
            orientation = Orientation.HORIZONTAL
        )
        val result = FleetRules.placeOrMove(
            fleet = listOf(carrier),
            type = ShipType.BATTLESHIP,
            coordinate = GridCoordinate(2, 0),
            orientation = Orientation.VERTICAL
        )
        assertNull(result)
    }

    @Test
    fun validShipMoveIsAccepted() {
        val fleet = listOf(
            PlacedShip(ShipType.CARRIER, 0, 0, Orientation.HORIZONTAL)
        )
        val result = FleetRules.placeOrMove(
            fleet = fleet,
            type = ShipType.BATTLESHIP,
            coordinate = GridCoordinate(5, 2),
            orientation = Orientation.VERTICAL
        )
        assertNotNull(result)
    }

    @Test
    fun shipOutsideGridIsRejected() {
        val result = FleetRules.placeOrMove(
            fleet = emptyList(),
            type = ShipType.CARRIER,
            coordinate = GridCoordinate(7, 9),
            orientation = Orientation.HORIZONTAL
        )
        assertNull(result)
    }

    @Test
    fun fleetIsLostOnlyWhenEveryShipCellWasHit() {
        val fleet = FleetRules.randomFleet(Random(47))
        val allHits = fleet
            .flatMap { it.cells() }
            .associateWith { ShotResult.HIT }
        val oneMissing = allHits - fleet.first().cells().first()

        assertTrue(fleet.allSunk(allHits))
        assertFalse(fleet.allSunk(oneMissing))
    }
}
