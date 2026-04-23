package com.nhom2.elearnlanguage.domain.model.speaking

data class AiRespondResult(
    val aiMessage: String,
    val aiMessageTranslation: String,
    val userHints: AiUserHints
)

