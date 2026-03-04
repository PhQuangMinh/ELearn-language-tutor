package com.nhom2.elearnlanguage.data.dto.lesson

import kotlinx.serialization.Serializable

@Serializable
data class MediaDTO(
    val id: Int,
    val name: String,
    val size: Int,
    val type: String, // "IMAGE" or "AUDIO"
    val url: String
)
