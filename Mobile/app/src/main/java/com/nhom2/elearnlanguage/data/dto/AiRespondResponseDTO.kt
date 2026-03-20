package com.nhom2.elearnlanguage.data.dto

data class AiRespondResponseDTO(
    val ai_message: String,
    val ai_message_translation: String,
    val user_hints: UserHintsDTO
)

