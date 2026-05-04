package com.glycin.koita.rest

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CreateUserRequest(
    val name: String,
    val score: Int,
    val email: String? = null,
)

@Serializable
data class HighscoreEntry(
    val name: String,
    val score: Int,
)

object ApiClient {

    private const val BASE_URL = "https://highscore-server-e4szw66yha-ew.a.run.app"

    private val client = HttpClient(Js) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun createUser(name: String, score: Int, email: String? = null) {
        client.post("$BASE_URL/users") {
            setBody(CreateUserRequest(name, score, email))
        }
    }

    suspend fun getHighscores(): List<HighscoreEntry> {
        return client.get("$BASE_URL/highscores").body()
    }
}
