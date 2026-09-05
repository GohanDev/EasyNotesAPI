package pt.ipt.easynotes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import pt.ipt.easynotes.database.NotesTable
import pt.ipt.easynotes.models.NoteRequest
import pt.ipt.easynotes.models.NoteResponse

fun Application.configureNotesRoutes() {

    routing {

        // Todas as operações sobre notas exigem autenticação JWT.
        authenticate("auth-jwt") {

            // Cria uma nova nota para o utilizador autenticado.
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

                val title = request.title.trim()
                val content = request.content.trim()

                // O título e o conteúdo são obrigatórios.
                if (title.isBlank() || content.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Título e conteúdo são obrigatórios.")
                    )
                    return@post
                }

                val noteId = transaction {
                    NotesTable.insert {
                        it[NotesTable.userId] = userId
                        it[NotesTable.title] = title
                        it[NotesTable.content] = content
                    } get NotesTable.id
                }

                call.respond(
                    HttpStatusCode.Created,
                    NoteResponse(
                        id = noteId,
                        title = title,
                        content = content
                    )
                )
            }

            // Devolve apenas as notas pertencentes ao utilizador autenticado.
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

            // Altera uma nota apenas se esta pertencer ao utilizador autenticado.
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

                val title = request.title.trim()
                val content = request.content.trim()

                // Também é feita validação quando uma nota é editada.
                if (title.isBlank() || content.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Título e conteúdo são obrigatórios.")
                    )
                    return@put
                }

                // O userId impede um utilizador de alterar notas de outro.
                val updatedRows = transaction {
                    NotesTable.update({
                        (NotesTable.id eq noteId) and
                                (NotesTable.userId eq userId)
                    }) {
                        it[NotesTable.title] = title
                        it[NotesTable.content] = content
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
                        title = title,
                        content = content
                    )
                )
            }

            // Elimina uma nota apenas se pertencer ao utilizador autenticado.
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

                // O userId impede a eliminação de notas de outros utilizadores.
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