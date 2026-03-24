package com.nhom2.elearnlanguage.presentation.ui.main_app.home.speaking

data class ConversationContext(
    val title: String,
    val scenario: String,
    val mission: List<String>
)

data class Hint(
    val analysis: String,
    val suggestion: String,
    val example: String
)

data class Message(
    val id: String,
    val text: String,
    val translation: String?,
    val isFromAI: Boolean,
    val hint: Hint?,
    val improveState: ImproveState = ImproveState.NONE,
    val improveResult: ImproveResult? = null
)

enum class ImproveState {
    NONE,
    LOADING,
    READY,
    ERROR
}

data class ImproveResult(
    val original: String,
    val improved: String,
    val explanation: String
)

data class ConversationUiState(
    val messages: List<Message>,
    val expandedHintMessageId: String?
)

