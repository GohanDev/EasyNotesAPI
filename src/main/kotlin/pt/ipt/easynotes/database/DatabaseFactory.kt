package pt.ipt.easynotes.database

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.net.URI

object DatabaseFactory {

    // Configura a base de dados utilizada pela API.
    fun init() {

        val databaseUrl =
            System.getenv("DATABASE_URL")

        if (databaseUrl.isNullOrBlank()) {

            // Sem DATABASE_URL é utilizada uma base de dados H2 local.
            Database.connect(
                url = "jdbc:h2:file:./data/easynotes",
                driver = "org.h2.Driver"
            )

        } else {

            // Em produção a ligação PostgreSQL é obtida da variável DATABASE_URL.
            val uri = URI(databaseUrl)

            val userInfo =
                uri.userInfo.split(":", limit = 2)

            val username = userInfo[0]
            val password = userInfo[1]

            val jdbcUrl =
                "jdbc:postgresql://" +
                        uri.host +
                        ":" +
                        uri.port +
                        uri.path

            Database.connect(
                url = jdbcUrl,
                driver = "org.postgresql.Driver",
                user = username,
                password = password
            )
        }

        // Cria as tabelas caso ainda não existam.
        transaction {
            SchemaUtils.create(
                UsersTable,
                NotesTable
            )
        }
    }
}