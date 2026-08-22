package pt.ipt.easynotes

import io.ktor.server.application.Application
import pt.ipt.easynotes.database.DatabaseFactory

fun Application.module() {

    DatabaseFactory.init()

    configureSerialization()
    configureRouting()
    configureAuthRoutes()
}