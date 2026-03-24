package com.nhom2.elearnlanguage.presentation.ui.main_app.home.speaking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhom2.elearnlanguage.domain.model.speaking.AiRespondRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.nhom2.elearnlanguage.domain.model.speaking.LessonScenario
import com.nhom2.elearnlanguage.domain.usecase.AiRespondUseCase
import com.nhom2.elearnlanguage.domain.usecase.EndSpeakingSessionUseCase
import com.nhom2.elearnlanguage.domain.usecase.GetScenarioByLessonUseCase
import com.nhom2.elearnlanguage.domain.usecase.ImproveMessageUseCase
import com.nhom2.elearnlanguage.domain.usecase.InitSpeakingSessionUseCase
import com.nhom2.elearnlanguage.domain.usecase.ObserveSpeechUseCase
import com.nhom2.elearnlanguage.domain.usecase.StartListeningUseCase
import com.nhom2.elearnlanguage.domain.usecase.StopListeningUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

@HiltViewModel
class AiConversationViewModel @Inject constructor(
    private val getScenarioByLessonUseCase: GetScenarioByLessonUseCase,
    private val initSpeakingSessionUseCase: InitSpeakingSessionUseCase,
    private val endSpeakingSessionUseCase: EndSpeakingSessionUseCase,
    private val aiRespondUseCase: AiRespondUseCase,
    private val improveMessageUseCase: ImproveMessageUseCase,
    private val startListeningUseCase: StartListeningUseCase,
    private val stopListeningUseCase: StopListeningUseCase,
    private val observeSpeechUseCase: ObserveSpeechUseCase
) : ViewModel() {

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    init {
        viewModelScope.launch {
            observeSpeechUseCase().collect {
                _text.value = it
            }
        }
    }

    fun toggleListening() {
        if (_isListening.value) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun startListening() {
        _isListening.value = true
        startListeningUseCase()
    }

    fun stopListening() {
        _isListening.value = false
        stopListeningUseCase()
    }

    fun clearText() {
        _text.value = ""
    }

    private val initialContext = ConversationContext(
        title = "",
        scenario = "",
        mission = emptyList()
    )

    private val _context = MutableStateFlow(initialContext)
    val context: StateFlow<ConversationContext> = _context

    private val _uiState = MutableStateFlow(
        ConversationUiState(
            messages = emptyList(),
            expandedHintMessageId = null
        )
    )
    val uiState: StateFlow<ConversationUiState> = _uiState

    private val _openImproveSheet = MutableSharedFlow<ImproveResult>(extraBufferCapacity = 1)
    val openImproveSheet: SharedFlow<ImproveResult> = _openImproveSheet

    private var hasStarted = false
    private var scenarioId: Int? = null
    private var speakingSessionId: Int? = null

    fun start(lessonId: Int) {
        if (hasStarted) return
        hasStarted = true

        viewModelScope.launch {
            val scenario = try {
                // API1: lấy scenario theo lesson (start hội thoại phần trên)
                getScenarioByLessonUseCase(lessonId)
            } catch (_: Exception) {
                _context.value = initialContext
                _uiState.value = ConversationUiState(
                    messages = emptyList(),
                    expandedHintMessageId = null
                )
                return@launch
            }

            scenarioId = scenario.id
            speakingSessionId = try {
                initSpeakingSessionUseCase(scenario.id)
            } catch (_: Exception) {
                null
            }

            applyScenario(scenario)
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _isSending.value) return

        _isSending.value = true

        val currentState = _uiState.value
        val conversationHistory = buildConversationHistory(currentState.messages)

        val userMessageId = addUserMessage(trimmed)
        clearText()
        userMessageId?.let { messageId ->
            requestImproveForMessage(messageId = messageId, originalText = trimmed)
        }

        viewModelScope.launch {
            val sessionId = speakingSessionId
            if (sessionId == null) {
                addAiMessage(
                    aiMessage = "I can't start the speaking session right now. Please try again.",
                    aiTranslation = "Mình chưa khởi tạo được phiên nói. Bạn thử lại giúp mình nhé.",
                    userHint = null
                )
                _isSending.value = false
                return@launch
            }

            try {
                val result = aiRespondUseCase(
                    AiRespondRequest(
                        speakingSessionId = sessionId,
                        scenarioDescription = _context.value.scenario,
                        taskDescription = _context.value.mission.joinToString(separator = "\n"),
                        conversationHistory = conversationHistory,
                        userMessage = trimmed
                    )
                )

                addAiMessage(
                    aiMessage = result.aiMessage,
                    aiTranslation = result.aiMessageTranslation,
                    userHint = Hint(
                        analysis = result.userHints.analysis,
                        suggestion = result.userHints.suggestion,
                        example = result.userHints.example
                    )
                )
            } catch (_: Exception) {
                addAiMessage(
                    aiMessage = "I’m having trouble responding right now. Please try again.",
                    aiTranslation = "Mình đang gặp lỗi khi phản hồi. Bạn thử lại nhé.",
                    userHint = null
                )
            } finally {
                _isSending.value = false
            }
        }
    }

    fun endSession() {
        val sessionId = speakingSessionId ?: return
        speakingSessionId = null
        viewModelScope.launch {
            try {
                endSpeakingSessionUseCase(sessionId)
            } catch (_: Exception) {
            }
        }
    }

    fun toggleHint(messageId: String) {
        _uiState.update { state ->
            val next = if (state.expandedHintMessageId == messageId) null else messageId
            state.copy(expandedHintMessageId = next)
        }
    }

    private fun applyScenario(scenario: LessonScenario) {
        val missions = parseTasksToMissionList(scenario.tasks)
        val newContext = ConversationContext(
            title = scenario.title,
            scenario = scenario.description,
            mission = missions
        )

        _context.value = newContext

        val openingAi = Message(
            id = genId("ai_opening"),
            text = scenario.openingMessage,
            translation = scenario.translation,
            isFromAI = true,
            // Để nút "Gợi ý" hiển thị ngay ở message AI đầu tiên (mặc định BE chỉ có suggestion cho scenario).
            hint = if (scenario.suggestion.isNullOrBlank()) {
                null
            } else {
                Hint(
                    analysis = "",
                    suggestion = scenario.suggestion,
                    example = ""
                )
            }
        )

        _uiState.value = ConversationUiState(
            messages = listOf(openingAi),
            expandedHintMessageId = null
        )
    }

    /**
     * Bottom UI (bạn ghép sau) sẽ call API2 và sau đó đẩy message vào UI thông qua các hàm này.
     * Top UI của bạn chỉ gọi API1 nên ViewModel ở đây không tự gọi aiRespond nữa.
     */
    fun addUserMessage(text: String): String? {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return null

        val userBubble = Message(
            id = genId("u"),
            text = trimmed,
            translation = null,
            isFromAI = false,
            hint = null
        )

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userBubble,
                expandedHintMessageId = null
            )
        }
        return userBubble.id
    }

    fun addAiMessage(aiMessage: String, aiTranslation: String?, userHint: Hint?) {
        val trimmed = aiMessage.trim()
        if (trimmed.isBlank()) return

        val aiBubble = Message(
            id = genId("ai"),
            text = trimmed,
            translation = aiTranslation,
            isFromAI = true,
            hint = userHint
        )

        _uiState.update { state ->
            state.copy(
                messages = state.messages + aiBubble,
                expandedHintMessageId = null
            )
        }
    }

    fun onImproveClick(messageId: String) {
        val message = _uiState.value.messages.firstOrNull { it.id == messageId } ?: return
        if (message.isFromAI) return

        when (message.improveState) {
            ImproveState.READY -> {
                message.improveResult?.let { _openImproveSheet.tryEmit(it) }
            }
            ImproveState.ERROR, ImproveState.NONE -> {
                requestImproveForMessage(
                    messageId = message.id,
                    originalText = message.text
                )
            }
            ImproveState.LOADING -> Unit
        }
    }

    private fun requestImproveForMessage(messageId: String, originalText: String) {
        updateMessageImproveState(messageId, ImproveState.LOADING, null)
        viewModelScope.launch {
            val result = runCatching {
                improveMessageUseCase(
                    text = originalText,
                    context = buildImproveContext()
                )
            }.getOrNull()

            if (result == null) {
                updateMessageImproveState(messageId, ImproveState.ERROR, null)
                return@launch
            }

            val mapped = ImproveResult(
                original = result.original,
                improved = result.improved,
                explanation = result.explanation
            )
            updateMessageImproveState(messageId, ImproveState.READY, mapped)
        }
    }

    private fun updateMessageImproveState(
        messageId: String,
        state: ImproveState,
        result: ImproveResult?
    ) {
        _uiState.update { current ->
            current.copy(
                messages = current.messages.map { message ->
                    if (message.id == messageId && !message.isFromAI) {
                        message.copy(improveState = state, improveResult = result)
                    } else {
                        message
                    }
                }
            )
        }
    }

    private fun parseTasksToMissionList(tasks: String): List<String> {
        val normalized = tasks
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .trim()
        if (normalized.isBlank()) return emptyList()

        // Handle JSON-like array string:
        // ["Where is your hometown","What is it famous for","Do you like it"]
        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            val quotedItems = Regex("\"([^\"]+)\"")
                .findAll(normalized)
                .map { it.groupValues[1].trim() }
                .filter { it.isNotBlank() }
                .toList()
            if (quotedItems.isNotEmpty()) return quotedItems

            val csvItems = normalized
                .removePrefix("[")
                .removeSuffix("]")
                .split(",")
                .map { it.trim().trim('"') }
                .filter { it.isNotBlank() }
            if (csvItems.isNotEmpty()) return csvItems
        }

        val byLines = normalized
            .split("\n")
            .map { it.trim().trimStart('•', '-', '*').trim() }
            .filter { it.isNotBlank() }

        if (byLines.size >= 2) return byLines

        return normalized
            .split(Regex("[;•]"))
            .map { it.trim().trimStart('•', '-', '*', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '.') }
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun genId(prefix: String): String {
        return "${prefix}_${System.nanoTime()}"
    }

    private fun buildImproveContext(): String {
        val ctx = _context.value
        val mission = ctx.mission.joinToString(separator = "; ")
        val merged = listOf(ctx.title, ctx.scenario, mission)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(separator = " | ")
        return merged.ifBlank { "General English conversation practice." }
    }

    private fun buildConversationHistory(messages: List<Message>): String {
        return messages.joinToString(separator = "\n") { message ->
            val role = if (message.isFromAI) "AI" else "USER"
            "$role: ${message.text}"
        }
    }
}

