package pt.ipt.easynotes

import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    routing {

        // Disponibiliza a documentação Swagger da API.
        swaggerUI(
            path = "swagger",
            swaggerFile = "openapi.yaml"
        )

        // Rota principal utilizada para identificar a API.
        get("/") {
            call.respondText("EasyNotes API")
        }

        // Permite verificar se a API está disponível.
        get("/health") {
            call.respond(
                mapOf(
                    "status" to "ok"
                )
            )
        }
    }
}