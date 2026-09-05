package pt.ipt.easynotes.models

import kotlinx.serialization.Serializable

// Dados recebidos pela API para criar ou editar uma nota.
@Serializable
data class NoteRequest(
    val title: String,
    val content: String
)

// Dados de uma nota devolvidos pela API.
@Serializable
data class NoteResponse(
    val id: Int,
    val title: String,
    val content: String
)