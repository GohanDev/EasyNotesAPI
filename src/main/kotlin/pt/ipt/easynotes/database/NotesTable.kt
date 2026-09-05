package pt.ipt.easynotes.database

import org.jetbrains.exposed.v1.core.Table

// Tabela que guarda as notas dos utilizadores.
object NotesTable : Table("notes") {

    val id = integer("id").autoIncrement()

    // Cada nota fica associada ao utilizador que a criou.
    val userId = integer("user_id")
        .references(UsersTable.id)

    val title = varchar("title", 200)
    val content = text("content")

    override val primaryKey = PrimaryKey(id)
}