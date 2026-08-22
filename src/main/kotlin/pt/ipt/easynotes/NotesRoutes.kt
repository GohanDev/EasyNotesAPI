package pt.ipt.easynotes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import pt.ipt.easynotes.database.NotesTable
import pt.ipt.easynotes.models.NoteRequest
import pt.ipt.easynotes.models.NoteResponse
import io.ktor.server.routing.get
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import io.ktor.server.routing.put
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.core.and
import io.ktor.server.routing.delete
import org.jetbrains.exposed.v1.jdbc.deleteWhere

fun Application.configureNotesRoutes() {

    routing {

        authenticate("auth-jwt") {

            post("/notes") {

                val principal = call.principal<JWTPrincipal>()

                val userId = principal
                    ?.payload
                    ?.getClaim("userId")
                    ?.asInt()

                if (userId == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Utilizador não autenticado.")
                    )
                    return@post
                }

                val request = call.receive<NoteRequest>()

                if (
                    request.title.isBlank() ||
                    request.content.isBlank()
                ) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Título e conteúdo são obrigatórios.")
                    )
                    return@post
                }

                val noteId = transaction {

                    NotesTable.insert {
                        it[NotesTable.userId] = userId
                        it[title] = request.title
                        it[content] = request.content
                    } get NotesTable.id
                }

                call.respond(
                    HttpStatusCode.Created,
                    NoteResponse(
                        id = noteId,
                        title = request.title,
                        content = request.content
                    )
                )
            }

            get("/notes") {

                val principal = call.principal<JWTPrincipal>()

                val userId = principal
                    ?.payload
                    ?.getClaim("userId")
                    ?.asInt()

                if (userId == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Utilizador não autenticado.")
                    )
                    return@get
                }

                val notes = transaction {

                    NotesTable
                        .selectAll()
                        .where {
                            NotesTable.userId eq userId
                        }
                        .map { row ->
                            NoteResponse(
                                id = row[NotesTable.id],
                                title = row[NotesTable.title],
                                content = row[NotesTable.content]
                            )
                        }
                }

                call.respond(
                    HttpStatusCode.OK,
                    notes
                )
            }

            put("/notes/{id}") {

                val principal = call.principal<JWTPrincipal>()

                val userId = principal
                    ?.payload
                    ?.getClaim("userId")
                    ?.asInt()

                if (userId == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Utilizador não autenticado.")
                    )
                    return@put
                }

                val noteId = call.parameters["id"]?.toIntOrNull()

                if (noteId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "ID da nota inválido.")
                    )
                    return@put
                }

                val request = call.receive<NoteRequest>()

                if (
                    request.title.isBlank() ||
                    request.content.isBlank()
                ) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Título e conteúdo são obrigatórios.")
                    )
                    return@put
                }

                val updatedRows = transaction {

                    NotesTable.update({
                        (NotesTable.id eq noteId) and
                                (NotesTable.userId eq userId)
                    }) {
                        it[title] = request.title
                        it[content] = request.content
                    }
                }

                if (updatedRows == 0) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "Nota não encontrada.")
                    )
                    return@put
                }

                call.respond(
                    HttpStatusCode.OK,
                    NoteResponse(
                        id = noteId,
                        title = request.title,
                        content = request.content
                    )
                )
            }

            delete("/notes/{id}") {

                val principal = call.principal<JWTPrincipal>()

                val userId = principal
                    ?.payload
                    ?.getClaim("userId")
                    ?.asInt()

                if (userId == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Utilizador não autenticado.")
                    )
                    return@delete
                }

                val noteId = call.parameters["id"]?.toIntOrNull()

                if (noteId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "ID da nota inválido.")
                    )
                    return@delete
                }

                val deletedRows = transaction {

                    NotesTable.deleteWhere {
                        (NotesTable.id eq noteId) and
                                (NotesTable.userId eq userId)
                    }
                }

                if (deletedRows == 0) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "Nota não encontrada.")
                    )
                    return@delete
                }

                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Nota apagada com sucesso.")
                )
            }
        }
    }
}