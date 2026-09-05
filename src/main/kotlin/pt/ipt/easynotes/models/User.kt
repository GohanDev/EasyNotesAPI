package pt.ipt.easynotes.models

import kotlinx.serialization.Serializable

// Dados necessários para registar um novo utilizador.
@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

// Dados necessários para autenticar um utilizador.
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

// Dados públicos do utilizador devolvidos pela API.
// A password e o respetivo hash nunca são enviados ao cliente.
@Serializable
data class UserResponse(
    val id: Int,
    val name: String,
    val email: String
)

// Resposta enviada após um login efetuado com sucesso.
@Serializable
data class LoginResponse(
    val token: String,
    val user: UserResponse
)