package ch.davide.pham.battleshipproject

import ch.davide.pham.battleshipproject.model.EnemyFireRequest
import ch.davide.pham.battleshipproject.model.EnemyFireResponse
import ch.davide.pham.battleshipproject.model.FireRequest
import ch.davide.pham.battleshipproject.model.FireResponse
import ch.davide.pham.battleshipproject.model.JoinRequest
import ch.davide.pham.battleshipproject.model.JoinResponse
import ch.davide.pham.battleshipproject.model.PingResponse
import ch.davide.pham.battleshipproject.network.BattleshipApi
import ch.davide.pham.battleshipproject.network.BattleshipRepository
import ch.davide.pham.battleshipproject.network.NetworkResult
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class BattleshipRepositoryTest {
    private val joinRequest = JoinRequest(
        player = "Davide",
        gameKey = "FLEET47",
        ships = emptyList()
    )

    @Test
    fun errorEnvelopeInsideHttp200BecomesFailure() = runBlocking {
        val api = FakeApi(
            joinResponse = Response.success(JoinResponse(error = "Not this player's turn"))
        )

        val result = BattleshipRepository(api).join(joinRequest)

        assertTrue(result is NetworkResult.Failure)
        assertEquals(
            "Not this player's turn",
            (result as NetworkResult.Failure).message
        )
    }

    @Test
    fun invalidMappingHttpStatusBecomesFailure() = runBlocking {
        val errorBody = """{"Error":"Invalid mapping"}"""
            .toResponseBody("application/json".toMediaType())
        val api = FakeApi(
            joinResponse = Response.error(418, errorBody)
        )

        val result = BattleshipRepository(api).join(joinRequest)

        assertTrue(result is NetworkResult.Failure)
        assertTrue((result as NetworkResult.Failure).message.contains("HTTP 418"))
    }

    private class FakeApi(
        private val joinResponse: Response<JoinResponse>
    ) : BattleshipApi {
        override suspend fun ping(): Response<PingResponse> =
            Response.success(PingResponse(true))

        override suspend fun joinGame(request: JoinRequest): Response<JoinResponse> =
            joinResponse

        override suspend fun fire(request: FireRequest): Response<FireResponse> =
            Response.success(FireResponse())

        override suspend fun enemyFire(
            request: EnemyFireRequest
        ): Response<EnemyFireResponse> =
            Response.success(EnemyFireResponse())
    }
}
