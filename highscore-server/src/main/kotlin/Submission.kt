package com.glycin

import com.glycin.model.CreateUserRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.minutes

private val NAME_REGEX = Regex("^[A-Za-z0-9 _]{1,16}$")

private val ALLOWED_ORIGINS = listOf(
    "https://improved-broccoli-v31e8gl.pages.github.io",
    "https://game.kotlinlang.org",
    "http://localhost:8080",
)

val SUBMIT_LIMITER = RateLimitName("submit")

fun Application.configureRateLimit() {
    install(RateLimit) {
        register(SUBMIT_LIMITER) {
            rateLimiter(limit = 5, refillPeriod = 1.minutes)
            requestKey { call ->
                call.request.headers[HttpHeaders.XForwardedFor]?.substringBefore(",")?.trim()
                    ?: call.request.origin.remoteHost
            }
        }
    }
}

fun ApplicationCall.originAllowed(): Boolean {
    val origin = request.headers[HttpHeaders.Origin]
    if (origin != null) return origin in ALLOWED_ORIGINS
    val referer = request.headers["Referer"] ?: return false
    return ALLOWED_ORIGINS.any { referer == it || referer.startsWith("$it/") }
}

enum class SubmissionRejection { HONEYPOT, NAME }

fun validateSubmission(request: CreateUserRequest): SubmissionRejection? = when {
    !request.website.isNullOrBlank() -> SubmissionRejection.HONEYPOT
    !NAME_REGEX.matches(request.name) -> SubmissionRejection.NAME
    else -> null
}
