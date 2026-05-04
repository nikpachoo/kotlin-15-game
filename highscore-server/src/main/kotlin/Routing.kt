package com.glycin

import com.glycin.model.CreateUserRequest
import com.glycin.model.HighscoreEntry
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private const val HONEYPOT_FAKE_ID = 0

fun Application.configureRouting(userService: UserService) {
    routing {
        get("/highscores") {
            val topScores = userService.topHighscores()
            val highscores = topScores.map { HighscoreEntry(
                name = it.name,
                score = it.score
            ) }
            call.respond(HttpStatusCode.OK, highscores)
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
                        call.respond(HttpStatusCode.Created, HONEYPOT_FAKE_ID)
                        return@post
                    }
                    SubmissionRejection.NAME -> {
                        call.respond(HttpStatusCode.BadRequest)
                        return@post
                    }
                    null -> {}
                }
                val id = userService.create(ExposedUser(request.name, request.score, request.email))
                call.respond(HttpStatusCode.Created, id)
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
