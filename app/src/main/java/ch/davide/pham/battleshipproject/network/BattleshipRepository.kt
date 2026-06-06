package ch.davide.pham.battleshipproject.network

import android.util.Log
import ch.davide.pham.battleshipproject.model.EnemyFireRequest
import ch.davide.pham.battleshipproject.model.EnemyFireResponse
import ch.davide.pham.battleshipproject.model.FireRequest
import ch.davide.pham.battleshipproject.model.FireResponse
import ch.davide.pham.battleshipproject.model.JoinRequest
import ch.davide.pham.battleshipproject.model.JoinResponse
import ch.davide.pham.battleshipproject.model.PingResponse
import ch.davide.pham.battleshipproject.model.ServerEnvelope
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException

/*
 * Author: Davide Pham
 * Repository for official Battleship REST server communication.
 * Updated with strict JSON logging and error handling for /game/join.
 */
sealed interface NetworkResult<out T> {
    data class Success<T>(val value: T) : NetworkResult<T>
    data class Failure(val message: String) : NetworkResult<Nothing>
}

open class BattleshipRepository(
    private val api: BattleshipApi = RetrofitClient.api
) {
    private val TAG = "BattleshipRepo"
    private val gson = Gson()

    open suspend fun ping(): NetworkResult<PingResponse> {
        return try {
            val response = api.ping()
            val rawRequest = response.raw().request
            val url = rawRequest.url.toString()
            val code = response.code()
            val body = response.body()
            
            Log.d(TAG, "PING Request URL: $url")
            Log.d(TAG, "PING HTTP Response Code: $code")
            
            if (response.isSuccessful && body != null) {
                Log.d(TAG, "PING Response Body: $body")
                NetworkResult.Success(body)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.d(TAG, "PING Error Message: $errorMsg")
                NetworkResult.Failure("Server not reachable (HTTP $code)")
            }
        } catch (io: IOException) {
            Log.d(TAG, "PING Error Message: ${io.message}")
            NetworkResult.Failure("Server not reachable")
        } catch (e: Exception) {
            Log.d(TAG, "PING Error Message: ${e.message}")
            NetworkResult.Failure(e.message ?: "Unexpected error")
        }
    }

    open suspend fun join(request: JoinRequest): NetworkResult<JoinResponse> {
        Log.d(TAG, "JOIN Request Body: ${gson.toJson(request)}")
        return execute(
            request = { api.joinGame(request) },
            serverError = { it.error },
            tag = "JOIN"
        )
    }

    open suspend fun fire(request: FireRequest): NetworkResult<FireResponse> {
        Log.d(TAG, "FIRE Request Body: ${gson.toJson(request)}")
        return execute(
            request = { api.fire(request) },
            serverError = { it.error },
            tag = "FIRE"
        )
    }

    open suspend fun enemyFire(request: EnemyFireRequest): NetworkResult<EnemyFireResponse> {
        Log.d(TAG, "ENEMY_FIRE Request Body: ${gson.toJson(request)}")
        return execute(
            request = { api.enemyFire(request) },
            serverError = { it.error },
            tag = "ENEMY_FIRE"
        )
    }

    private suspend fun <T> execute(
        request: suspend () -> Response<T>,
        serverError: (T) -> String? = { (it as? ServerEnvelope)?.error },
        tag: String = "API"
    ): NetworkResult<T> {
        return try {
            val response = request()
            val body = response.body()
            val code = response.code()
            
            Log.d(TAG, "$tag HTTP Response Code: $code")
            
            when {
                !response.isSuccessful -> {
                    val detail = response.errorBody()?.string()?.trim()?.take(180)
                    Log.d(TAG, "$tag Error Response Body: $detail")
                    val suffix = if (detail.isNullOrBlank()) "" else ": $detail"
                    NetworkResult.Failure("Server rejected the request (HTTP $code)$suffix")
                }

                body == null -> {
                    Log.d(TAG, "$tag Response Body: NULL")
                    NetworkResult.Failure("The server returned an empty response.")
                }
                
                !serverError(body).isNullOrBlank() -> {
                    val error = serverError(body)!!
                    Log.d(TAG, "$tag Protocol Error: $error")
                    NetworkResult.Failure(error)
                }
                
                else -> {
                    Log.d(TAG, "$tag Response Body: ${gson.toJson(body)}")
                    NetworkResult.Success(body)
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (io: IOException) {
            Log.d(TAG, "$tag IO Exception: ${io.message}")
            NetworkResult.Failure(
                "Cannot reach the Battleship server. Check your connection and try again."
            )
        } catch (error: Exception) {
            Log.d(TAG, "$tag Unexpected Error: ${error.message}")
            NetworkResult.Failure(error.message ?: "Unexpected network error.")
        }
    }
}
