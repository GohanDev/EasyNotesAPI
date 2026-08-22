package pt.ipt.easynotes.database

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {

    fun init() {

        Database.connect(
            url = "jdbc:h2:file:./data/easynotes",
            driver = "org.h2.Driver"
        )

        transaction {
            SchemaUtils.create(
                UsersTable,
                NotesTable
            )
        }
    }
}