package com.glycin

import com.glycin.model.CreateUserRequest
import com.glycin.model.HighscoreEntry
import com.glycin.model.HighscoresResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(userService: UserService) {
    routing {
        get("/highscores") {
            call.respond(HttpStatusCode.OK, userService.leaderboard().toResponse())
        }

        rateLimit(SUBMIT_LIMITER) {
            post("/users") {
                if (!call.originAllowed()) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@post
                }
                val request = call.receive<CreateUserRequest>()
                when (validateSubmission(request)) {
                    SubmissionRejection.HONEYPOT -> {
                        call.respond(HttpStatusCode.Created, userService.leaderboard().toResponse())
                        return@post
                    }
                    SubmissionRejection.NAME -> {
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }
                    null -> {}
                }
                val result = userService.submit(ExposedUser(request.name, request.score, request.email))
                val userEntry = if (result.rank > result.snapshot.top.size) {
                    HighscoreEntry(name = request.name, score = request.score, rank = result.rank)
                } else null
                call.respond(HttpStatusCode.Created, result.snapshot.toResponse(userEntry))
            }
        }

        authenticate("auth-basic") {
            get("/") {
                call.respondText("Hello World!")
            }

            get("/users/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val user = userService.read(id)
                if (user != null) {
                    call.respond(HttpStatusCode.OK, user)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            put("/users/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val user = call.receive<ExposedUser>()
                userService.update(id, user)
                call.respond(HttpStatusCode.OK)
            }

            delete("/users/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                userService.delete(id)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

private fun LeaderboardSnapshot.toResponse(userEntry: HighscoreEntry? = null) =
    HighscoresResponse(
        top = top.mapIndexed { index, user ->
            HighscoreEntry(name = user.name, score = user.score, rank = index + 1)
        },
        totalEntries = total,
        userEntry = userEntry,
    )
