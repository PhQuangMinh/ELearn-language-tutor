package com.nhom2.elearnlanguage.data.dto.lesson

import kotlinx.serialization.Serializable

@Serializable
data class AnswerDTO(
    val id: Int,
    val content: String,
    val correct: Boolean
)
