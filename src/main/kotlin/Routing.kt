package pt.ipt.easynotes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.swagger.swaggerUI
fun Application.configureRouting() {

    routing {

        swaggerUI(
            path = "swagger",
            swaggerFile = "openapi.yaml"
        )

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