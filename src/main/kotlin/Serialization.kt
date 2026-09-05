package pt.ipt.easynotes

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

// Configura a API para receber e devolver dados em formato JSON.
fun Application.configureSerialization() {

    install(ContentNegotiation) {
        json()
    }
}