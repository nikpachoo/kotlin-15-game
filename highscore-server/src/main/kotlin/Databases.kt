package com.glycin

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*

fun Application.configureDatabases() {
    val url = environment.config.property("postgres.url").getString()
    val user = environment.config.property("postgres.user").getString()
    val password = environment.config.property("postgres.password").getString()

    log.info("Connecting to postgres: url=$url, user=$user, password=$password")

    val database = Database.connect(
        url = url,
        user = user,
        driver = "org.postgresql.Driver",
        password = password,
    )

    val userService = UserService(database)
    configureRouting(userService)
}
