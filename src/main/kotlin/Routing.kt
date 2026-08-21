package pt.ipt.easynotes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    routing {

        get("/") {
            call.respondText("EasyNotes API")
        }

        get("/health") {
            call.respond(
                mapOf(
                    "status" to "ok"
                )
            )
        }
    }
}