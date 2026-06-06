package ch.davide.pham.battleshipproject.network

import ch.davide.pham.battleshipproject.model.EnemyFireRequest
import ch.davide.pham.battleshipproject.model.EnemyFireResponse
import ch.davide.pham.battleshipproject.model.FireRequest
import ch.davide.pham.battleshipproject.model.FireResponse
import ch.davide.pham.battleshipproject.model.JoinRequest
import ch.davide.pham.battleshipproject.model.JoinResponse
import ch.davide.pham.battleshipproject.model.PingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/*
 * Author: Davide Pham (individual FHNW student project).
 * Mappings follow the REST interface in the supplied Battleship_Project.pdf.
 * OpenAI Codex assisted with implementation; see README.md for attribution.
 */
interface BattleshipApi {
    @GET("ping")
    suspend fun ping(): Response<PingResponse>

    @POST("game/join")
    suspend fun joinGame(@Body request: JoinRequest): Response<JoinResponse>

    @POST("game/fire")
    suspend fun fire(@Body request: FireRequest): Response<FireResponse>

    @POST("game/enemyFire")
    suspend fun enemyFire(@Body request: EnemyFireRequest): Response<EnemyFireResponse>
}
