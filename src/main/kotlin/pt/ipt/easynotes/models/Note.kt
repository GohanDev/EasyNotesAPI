package pt.ipt.easynotes.models

import kotlinx.serialization.Serializable

@Serializable
data class NoteRequest(
    val title: String,
    val content: String
)

@Serializable
data class NoteResponse(
    val id: Int,
    val title: String,
    val content: String
)