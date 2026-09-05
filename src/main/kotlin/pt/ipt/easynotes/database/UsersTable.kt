package pt.ipt.easynotes.database

import org.jetbrains.exposed.v1.core.Table

// Tabela que guarda os utilizadores registados.
object UsersTable : Table("users") {

    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)

    // O email é único para impedir contas duplicadas.
    val email = varchar("email", 150).uniqueIndex()

    // Guarda apenas o hash BCrypt da password.
    val passwordHash = varchar("password_hash", 255)

    override val primaryKey = PrimaryKey(id)
}