package com.glycin

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import java.util.Base64
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserIntegrationTest {

    companion object {
        private val postgres = PostgreSQLContainer("postgres:17").apply {
            start()
        }
    }

    private val testUsername = "admin"
    private val testPassword = "admin"
    private val basicAuthHeader = "Basic " + Base64.getEncoder().encodeToString("$testUsername:$testPassword".toByteArray())

    private fun ApplicationTestBuilder.configureTestApp() {
        environment {
            config = MapApplicationConfig(
                "postgres.url" to postgres.jdbcUrl,
                "postgres.user" to postgres.username,
                "postgres.password" to postgres.password,
                "auth.username" to testUsername,
                "auth.password" to testPassword,
            )
        }
        application {
            module()
        }
    }

    private fun ApplicationTestBuilder.jsonClient() = createClient {
        install(ContentNegotiation) {
            json()
        }
    }

    @Test
    fun `unauthenticated request returns 401`() = testApplication {
        configureTestApp()
        val response = client.get("/")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `wrong credentials returns 401`() = testApplication {
        configureTestApp()
        val wrongAuth = "Basic " + Base64.getEncoder().encodeToString("wrong:wrong".toByteArray())
        val response = client.get("/") {
            header(HttpHeaders.Authorization, wrongAuth)
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET root returns Hello World`() = testApplication {
        configureTestApp()
        val response = client.get("/") {
            header(HttpHeaders.Authorization, basicAuthHeader)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello World!", response.body<String>())
    }

    @Test
    fun `POST users creates user and returns ID`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, basicAuthHeader)
            setBody(ExposedUser("Alice", 100, "alice@test.com"))
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val id = response.body<Int>()
        assert(id > 0)
    }

    @Test
    fun `GET users by id returns created user`() = testApplication {
        configureTestApp()
        val client = jsonClient()

        val createResponse = client.post("/users") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, basicAuthHeader)
            setBody(ExposedUser("Bob", 200, "bob@test.com"))
        }
        val id = createResponse.body<Int>()

        val getResponse = client.get("/users/$id") {
            header(HttpHeaders.Authorization, basicAuthHeader)
        }
        assertEquals(HttpStatusCode.OK, getResponse.status)
        val user = getResponse.body<ExposedUser>()
        assertEquals("Bob", user.name)
        assertEquals(200, user.score)
        assertEquals("bob@test.com", user.email)
    }

    @Test
    fun `GET users by nonexistent id returns 404`() = testApplication {
        configureTestApp()
        val response = client.get("/users/999999") {
            header(HttpHeaders.Authorization, basicAuthHeader)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `PUT users updates user fields`() = testApplication {
        configureTestApp()
        val client = jsonClient()

        val createResponse = client.post("/users") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, basicAuthHeader)
            setBody(ExposedUser("Charlie", 50))
        }
        val id = createResponse.body<Int>()

        val updateResponse = client.put("/users/$id") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, basicAuthHeader)
            setBody(ExposedUser("Charlie Updated", 999, "charlie@test.com"))
        }
        assertEquals(HttpStatusCode.OK, updateResponse.status)

        val getResponse = client.get("/users/$id") {
            header(HttpHeaders.Authorization, basicAuthHeader)
        }
        val updated = getResponse.body<ExposedUser>()
        assertEquals("Charlie Updated", updated.name)
        assertEquals(999, updated.score)
        assertEquals("charlie@test.com", updated.email)
    }

    @Test
    fun `DELETE users removes user`() = testApplication {
        configureTestApp()
        val client = jsonClient()

        val createResponse = client.post("/users") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, basicAuthHeader)
            setBody(ExposedUser("DeleteMe", 0))
        }
        val id = createResponse.body<Int>()

        val deleteResponse = client.delete("/users/$id") {
            header(HttpHeaders.Authorization, basicAuthHeader)
        }
        assertEquals(HttpStatusCode.OK, deleteResponse.status)

        val getResponse = client.get("/users/$id") {
            header(HttpHeaders.Authorization, basicAuthHeader)
        }
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun `GET highscores returns users sorted by score descending`() = testApplication {
        configureTestApp()
        val client = jsonClient()

        val names = listOf("Low" to 10, "Mid" to 50, "High" to 100)
        for ((name, score) in names) {
            client.post("/users") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, basicAuthHeader)
                setBody(ExposedUser(name, score))
            }
        }

        val response = client.get("/highscores") {
            header(HttpHeaders.Authorization, basicAuthHeader)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val scores = response.body<List<ExposedUser>>()
        val scoreValues = scores.map { it.score }
        assertEquals(scoreValues, scoreValues.sortedDescending())
    }
}
