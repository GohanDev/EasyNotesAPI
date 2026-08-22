package pt.ipt.easynotes.database

import org.jetbrains.exposed.v1.core.Table

object UsersTable : Table("users") {

    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val email = varchar("email", 150).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)

    override val primaryKey = PrimaryKey(id)
}