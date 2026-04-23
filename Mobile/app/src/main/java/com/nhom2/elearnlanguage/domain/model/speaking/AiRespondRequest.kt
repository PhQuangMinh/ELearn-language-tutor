package com.nhom2.elearnlanguage.domain.model.speaking

data class AiRespondRequest(
    val speakingSessionId: Int,
    val scenarioDescription: String,
    val taskDescription: String,
    val conversationHistory: String,
    val userMessage: String
)

