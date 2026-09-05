package pt.ipt.easynotes

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import pt.ipt.easynotes.database.UsersTable
import pt.ipt.easynotes.models.LoginRequest
import pt.ipt.easynotes.models.LoginResponse
import pt.ipt.easynotes.models.RegisterRequest
import pt.ipt.easynotes.models.UserResponse

fun Application.configureAuthRoutes() {

    routing {

        // Registo de um novo utilizador.
        post("/auth/register") {

            val request = call.receive<RegisterRequest>()

            // Remove espaços desnecessários do nome e do email.
            val name = request.name.trim()
            val email = request.email.trim().lowercase()
            val password = request.password

            // Todos os campos são obrigatórios.
            if (
                name.isBlank() ||
                email.isBlank() ||
                password.isBlank()
            ) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Todos os campos são obrigatórios.")
                )
                return@post
            }

            // Validação simples do formato do email.
            val emailRegex = Regex(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            )

            if (!emailRegex.matches(email)) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "O email introduzido não é válido.")
                )
                return@post
            }

            // A password deve ter pelo menos 6 caracteres.
            if (password.length < 6) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "A password deve ter pelo menos 6 caracteres.")
                )
                return@post
            }

            // Verifica se já existe uma conta com o mesmo email.
            val existingUser = transaction {
                UsersTable
                    .selectAll()
                    .where {
                        UsersTable.email eq email
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

            // A password nunca é guardada diretamente.
            // É criado um hash BCrypt que será guardado na base de dados.
            val passwordHash = BCrypt.withDefaults()
                .hashToString(
                    12,
                    password.toCharArray()
                )

            // Guarda o novo utilizador na base de dados.
            val userId = transaction {
                UsersTable.insert {
                    it[UsersTable.name] = name
                    it[UsersTable.email] = email
                    it[UsersTable.passwordHash] = passwordHash
                } get UsersTable.id
            }

            call.respond(
                HttpStatusCode.Created,
                UserResponse(
                    id = userId,
                    name = name,
                    email = email
                )
            )
        }

        // Autenticação de um utilizador existente.
        post("/auth/login") {

            val request = call.receive<LoginRequest>()

            val email = request.email.trim().lowercase()
            val password = request.password

            // Impede pedidos de login com campos vazios.
            if (email.isBlank() || password.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Email e password são obrigatórios.")
                )
                return@post
            }

            // Procura o utilizador através do email.
            val user = transaction {
                UsersTable
                    .selectAll()
                    .where {
                        UsersTable.email eq email
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

            // Compara a password introduzida com o hash guardado.
            val passwordValid = BCrypt.verifyer()
                .verify(
                    password.toCharArray(),
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

            val userId = user[UsersTable.id]

            // Após autenticação válida é criado um token JWT.
            val token = generateToken(userId)

            call.respond(
                HttpStatusCode.OK,
                LoginResponse(
                    token = token,
                    user = UserResponse(
                        id = userId,
                        name = user[UsersTable.name],
                        email = user[UsersTable.email]
                    )
                )
            )
        }

        // Rotas dentro deste bloco exigem um JWT válido.
        authenticate("auth-jwt") {

            // Devolve os dados do utilizador autenticado.
            get("/me") {

                val principal = call.principal<JWTPrincipal>()

                // Obtém o ID do utilizador que foi colocado no token JWT.
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

                val user = transaction {
                    UsersTable
                        .selectAll()
                        .where {
                            UsersTable.id eq userId
                        }
                        .singleOrNull()
                }

                if (user == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "Utilizador não encontrado.")
                    )
                    return@get
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
}