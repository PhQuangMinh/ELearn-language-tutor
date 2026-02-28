package com.nhom2.elearnlanguage.domain.model.lesson

enum class MediaType {
    IMAGE,
    AUDIO
}

data class Media (
    val id: Int,
    val name: String,
    val size: Int,
    val type: MediaType,
    val url: String
)
