package ch.davide.pham.battleshipproject.model

import com.google.gson.annotations.SerializedName

/*
 * Author: Davide Pham (individual FHNW student project).
 * Protocol models follow the course specification in Battleship_Project.pdf.
 * Updated to include gameover field in all relevant responses for strict turn flow.
 */

data class PingResponse(
    @SerializedName("ping") val ping: Boolean = false
)

data class ShipPosition(
    @SerializedName("ship") val ship: String,
    @SerializedName("x") val x: Int,
    @SerializedName("y") val y: Int,
    @SerializedName("orientation") val orientation: String
)

data class JoinRequest(
    @SerializedName("player") val player: String,
    @SerializedName("gamekey") val gameKey: String,
    @SerializedName("ships") val ships: List<ShipPosition>
)

data class FireRequest(
    @SerializedName("player") val player: String,
    @SerializedName("gamekey") val gameKey: String,
    @SerializedName("x") val x: Int,
    @SerializedName("y") val y: Int
)

data class EnemyFireRequest(
    @SerializedName("player") val player: String,
    @SerializedName("gamekey") val gameKey: String
)

interface ServerEnvelope {
    val error: String?
}

data class JoinResponse(
    @SerializedName("x") val x: Int? = null,
    @SerializedName("y") val y: Int? = null,
    @SerializedName("gameover") val gameOver: Boolean = false,
    @SerializedName("Error") override val error: String? = null
) : ServerEnvelope

data class FireResponse(
    @SerializedName("hit") val hit: Boolean = false,
    @SerializedName("shipsSunk") val shipsSunk: List<String> = emptyList(),
    @SerializedName("gameover") val gameOver: Boolean = false,
    @SerializedName("Error") override val error: String? = null
) : ServerEnvelope

data class EnemyFireResponse(
    @SerializedName("x") val x: Int? = null,
    @SerializedName("y") val y: Int? = null,
    @SerializedName("gameover") val gameOver: Boolean = false,
    @SerializedName("Error") override val error: String? = null
) : ServerEnvelope
