package com.glycin

import com.glycin.model.CreateUserRequest
import com.glycin.model.HighscoresResponse
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import java.util.Base64
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserIntegrationTest {

    companion object {
        private val postgres = PostgreSQLContainer("postgres:17").apply {
            start()
        }
        private val testDatabase by lazy {
            Database.connect(postgres.jdbcUrl, user = postgres.username, password = postgres.password)
        }
    }

    private fun userIdByName(name: String): Int = transaction(testDatabase) {
        UserService.Users.selectAll()
            .where { UserService.Users.name eq name }
            .single()[UserService.Users.id]
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
        defaultRequest {
            header(HttpHeaders.Origin, "http://localhost:8080")
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
    fun `POST users is public and returns submission response with rank`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest("Alice", 100, "alice@test.com"))
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<HighscoresResponse>()
        val alice = body.top.singleOrNull { it.name == "Alice" && it.score == 100 }
        assertNotNull(alice)
        assert(alice.rank > 0)
        assertNull(body.userEntry)
        assert(body.totalEntries >= body.top.size.toLong())
        assert(body.totalEntries > 0)
    }

    @Test
    fun `GET users by id returns created user`() = testApplication {
        configureTestApp()
        val client = jsonClient()

        val createResponse = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest("Bob", 200, "bob@test.com"))
        }
        assertEquals(HttpStatusCode.Created, createResponse.status)
        val id = userIdByName("Bob")

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
    fun `GET users by id without auth returns 401`() = testApplication {
        configureTestApp()
        val response = client.get("/users/1")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
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
            setBody(CreateUserRequest("Charlie", 50))
        }
        assertEquals(HttpStatusCode.Created, createResponse.status)
        val id = userIdByName("Charlie")

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
    fun `PUT users without auth returns 401`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val response = client.put("/users/1") {
            contentType(ContentType.Application.Json)
            setBody(ExposedUser("Nope", 1))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `DELETE users removes user`() = testApplication {
        configureTestApp()
        val client = jsonClient()

        val createResponse = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest("DeleteMe", 0))
        }
        assertEquals(HttpStatusCode.Created, createResponse.status)
        val id = userIdByName("DeleteMe")

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
    fun `DELETE users without auth returns 401`() = testApplication {
        configureTestApp()
        val response = client.delete("/users/1")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST users without allowed origin returns 403`() = testApplication {
        configureTestApp()
        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"Eve","score":1}""")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `POST users with disallowed origin returns 403`() = testApplication {
        configureTestApp()
        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Origin, "https://evil.example.com")
            setBody("""{"name":"Eve","score":1}""")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `POST users with invalid name returns 400`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody(CreateUserRequest("<script>", 1))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST users with honeypot field silently succeeds without persisting`() = testApplication {
        configureTestApp()
        val client = jsonClient()
        val postResponse = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"Bot","score":1,"website":"http://spam.example"}""")
        }
        assertEquals(HttpStatusCode.Created, postResponse.status)
        val body = postResponse.body<HighscoresResponse>()
        assertNull(body.userEntry)
        assertEquals(emptyList(), body.top.filter { it.name == "Bot" })

        val highscores = client.get("/highscores").body<HighscoresResponse>()
        assertEquals(emptyList(), highscores.top.filter { it.name == "Bot" })
    }

    @Test
    fun `GET highscores is public and returns users sorted by score descending`() = testApplication {
        configureTestApp()
        val client = jsonClient()

        val names = listOf("Low" to 10, "Mid" to 50, "High" to 100)
        for ((name, score) in names) {
            client.post("/users") {
                contentType(ContentType.Application.Json)
                setBody(CreateUserRequest(name, score))
            }
        }

        val response = client.get("/highscores")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<HighscoresResponse>()
        val scoreValues = body.top.map { it.score }
        assertEquals(scoreValues, scoreValues.sortedDescending())
        assert(body.totalEntries >= body.top.size.toLong())
        assert(body.totalEntries > 0)
    }
}
