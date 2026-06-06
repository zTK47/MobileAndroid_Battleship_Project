package ch.davide.pham.battleshipproject

import ch.davide.pham.battleshipproject.model.EnemyFireRequest
import ch.davide.pham.battleshipproject.model.FireRequest
import ch.davide.pham.battleshipproject.model.FireResponse
import ch.davide.pham.battleshipproject.model.JoinRequest
import ch.davide.pham.battleshipproject.model.JoinResponse
import ch.davide.pham.battleshipproject.model.ShipPosition
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkModelsTest {
    private val gson = Gson()

    @Test
    fun joinRequestUsesExactProtocolFieldNames() {
        val json = gson.toJson(
            JoinRequest(
                player = "Davide",
                gameKey = "game47",
                ships = listOf(
                    ShipPosition("Carrier", 0, 0, "horizontal")
                )
            )
        )

        assertTrue(json.contains("\"gamekey\":\"game47\""))
        assertTrue(json.contains("\"orientation\":\"horizontal\""))
        assertFalse(json.contains("gameKey"))
    }

    @Test
    fun joinResponseReadsLowercaseGameover() {
        val response = gson.fromJson(
            """{"x":3,"y":7,"gameover":false}""",
            JoinResponse::class.java
        )

        assertEquals(3, response.x)
        assertEquals(7, response.y)
        assertFalse(response.gameOver)
    }

    @Test
    fun moveRequestsUseExactPlayerGamekeyAndCoordinateFields() {
        val fireJson = gson.toJson(FireRequest("Davide", "FLEET47", 9, 0))
        val enemyJson = gson.toJson(EnemyFireRequest("Davide", "FLEET47"))

        assertEquals(
            """{"player":"Davide","gamekey":"FLEET47","x":9,"y":0}""",
            fireJson
        )
        assertEquals(
            """{"player":"Davide","gamekey":"FLEET47"}""",
            enemyJson
        )
    }

    @Test
    fun fireResponseReadsShipsSunkArray() {
        val response = gson.fromJson(
            """{"hit":true,"shipsSunk":["PatrolBoat","Submarine"]}""",
            FireResponse::class.java
        )

        assertTrue(response.hit)
        assertEquals(listOf("PatrolBoat", "Submarine"), response.shipsSunk)
    }

    @Test
    fun serverErrorUsesCapitalizedErrorKey() {
        val response = gson.fromJson(
            """{"Error":"Not this player's turn"}""",
            FireResponse::class.java
        )

        assertEquals("Not this player's turn", response.error)
    }
}
