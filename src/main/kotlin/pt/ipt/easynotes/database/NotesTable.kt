package pt.ipt.easynotes.database

import org.jetbrains.exposed.v1.core.Table

object NotesTable : Table("notes") {

    val id = integer("id").autoIncrement()

    val userId = integer("user_id")
        .references(UsersTable.id)

    val title = varchar("title", 200)

    val content = text("content")

    override val primaryKey = PrimaryKey(id)
}