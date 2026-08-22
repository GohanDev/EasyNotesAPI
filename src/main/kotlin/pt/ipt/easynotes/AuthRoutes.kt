package pt.ipt.easynotes

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import pt.ipt.easynotes.database.UsersTable
import pt.ipt.easynotes.models.RegisterRequest
import pt.ipt.easynotes.models.UserResponse
import org.jetbrains.exposed.v1.core.eq
import pt.ipt.easynotes.models.LoginRequest

fun Application.configureAuthRoutes() {

    routing {

        post("/auth/register") {

            val request = call.receive<RegisterRequest>()

            if (
                request.name.isBlank() ||
                request.email.isBlank() ||
                request.password.isBlank()
            ) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Todos os campos são obrigatórios.")
                )
                return@post
            }

            val existingUser = transaction {
                UsersTable
                    .selectAll()
                    .where {
                        UsersTable.email eq request.email
                    }
                    .singleOrNull()
            }

            if (existingUser != null) {
                call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("error" to "Já existe um utilizador com este email.")
                )
                return@post
            }

            val passwordHash = BCrypt.withDefaults()
                .hashToString(
                    12,
                    request.password.toCharArray()
                )

            val userId = transaction {
                UsersTable.insert {
                    it[name] = request.name
                    it[email] = request.email
                    it[UsersTable.passwordHash] = passwordHash
                } get UsersTable.id
            }

            call.respond(
                HttpStatusCode.Created,
                UserResponse(
                    id = userId,
                    name = request.name,
                    email = request.email
                )
            )
        }

        post("/auth/login") {

            val request = call.receive<LoginRequest>()

            val user = transaction {
                UsersTable
                    .selectAll()
                    .where {
                        UsersTable.email eq request.email
                    }
                    .singleOrNull()
            }

            if (user == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Email ou password inválidos.")
                )
                return@post
            }

            val passwordHash = user[UsersTable.passwordHash]

            val passwordValid = BCrypt.verifyer()
                .verify(
                    request.password.toCharArray(),
                    passwordHash
                )
                .verified

            if (!passwordValid) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Email ou password inválidos.")
                )
                return@post
            }

            call.respond(
                HttpStatusCode.OK,
                UserResponse(
                    id = user[UsersTable.id],
                    name = user[UsersTable.name],
                    email = user[UsersTable.email]
                )
            )
        }
    }
}