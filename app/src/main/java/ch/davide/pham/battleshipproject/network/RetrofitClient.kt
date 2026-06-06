package ch.davide.pham.battleshipproject.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/*
 * Author: Davide Pham (individual FHNW student project).
 * Retrofit and OkHttp configuration.
 * Read timeout is set to 60s to support long-polling for /game/join and /game/enemyFire.
 */
object RetrofitClient {
    private const val BASE_URL = "http://brad-home.ch:50003/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        // Set a 60s timeout for long-polling. This ensures we don't wait forever
        // if the connection is dropped, but gives enough time for the opponent to act.
        .readTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    val api: BattleshipApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(BattleshipApi::class.java)
    }
}
