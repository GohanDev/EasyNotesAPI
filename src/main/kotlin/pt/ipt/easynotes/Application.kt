package pt.ipt.easynotes

import io.ktor.server.application.Application
import pt.ipt.easynotes.database.DatabaseFactory

// Ponto principal de configuração da API EasyNotes.
fun Application.module() {

    // Inicializa a base de dados.
    DatabaseFactory.init()

    // Configura a conversão dos dados para JSON.
    configureSerialization()

    // Configura a autenticação através de JWT.
    configureAuthentication()

    // Configura as rotas gerais da API.
    configureRouting()

    // Configura as rotas de registo, login e utilizador.
    configureAuthRoutes()

    // Configura o CRUD das notas.
    configureNotesRoutes()
}