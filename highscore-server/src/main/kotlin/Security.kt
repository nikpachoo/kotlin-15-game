package com.glycin

import io.ktor.server.application.*
import io.ktor.server.auth.*

fun Application.configureSecurity() {
    val expectedUsername = environment.config.property("auth.username").getString()
    val expectedPassword = environment.config.property("auth.password").getString()

    install(Authentication) {
        basic("auth-basic") {
            realm = "koita"
            validate { credentials ->
                if (credentials.name == expectedUsername && credentials.password == expectedPassword) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
}
