package pt.ipt.easynotes.database

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {

    fun init() {

        val databaseUrl = System.getenv("DATABASE_URL")

        if (databaseUrl.isNullOrBlank()) {

            // Desenvolvimento local com H2
            Database.connect(
                url = "jdbc:h2:file:./data/easynotes",
                driver = "org.h2.Driver"
            )

        } else {

            // Produção no Deployzy com PostgreSQL
            val jdbcUrl =
                databaseUrl.replace(
                    "postgresql://",
                    "jdbc:postgresql://"
                )

            Database.connect(
                url = jdbcUrl,
                driver = "org.postgresql.Driver"
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