package com.nhom2.elearnlanguage.data.dto

data class AiRespondRequestDTO(
    val speakingSessionId: Int,
    val scenarioDescription: String,
    val taskDescription: String,
    val conversationHistory: String,
    val userMessage: String
)

