package pt.ipt.easynotes.database

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.net.URI

object DatabaseFactory {

    fun init() {

        val databaseUrl =
            System.getenv("DATABASE_URL")

        if (databaseUrl.isNullOrBlank()) {

            // Desenvolvimento local com H2
            Database.connect(
                url = "jdbc:h2:file:./data/easynotes",
                driver = "org.h2.Driver"
            )

        } else {

            // Produção com PostgreSQL
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

        transaction {

            SchemaUtils.create(
                UsersTable,
                NotesTable
            )
        }
    }
}