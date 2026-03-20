package com.nhom2.elearnlanguage.presentation.ui.main_app.home.speaking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.nhom2.elearnlanguage.domain.model.speaking.AiRespondRequest
import com.nhom2.elearnlanguage.domain.model.speaking.LessonScenario
import com.nhom2.elearnlanguage.domain.usecase.AiRespondUseCase
import com.nhom2.elearnlanguage.domain.usecase.GetScenarioByLessonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AiConversationViewModel @Inject constructor(
    private val getScenarioByLessonUseCase: GetScenarioByLessonUseCase,
    private val aiRespondUseCase: AiRespondUseCase
) : ViewModel() {

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

    private var hasStarted = false

    fun start(lessonId: Int) {
        if (hasStarted) return
        hasStarted = true

        viewModelScope.launch {
            val scenario = try {
                getScenarioByLessonUseCase(lessonId)
            } catch (e: Exception) {
                // Nếu không lấy được scenario thì mới làm UI rỗng.
                _context.value = initialContext
                _uiState.value = ConversationUiState(
                    messages = emptyList(),
                    expandedHintMessageId = null
                )
                return@launch
            }

            // Lấy scenario xong thì UI context + phần "NHIỆM VỤ" sẽ được giữ lại
            // ngay cả khi call AI respond thất bại.
            applyScenario(scenario)

            try {
                sendAiRespond(userMessage = "Hello.")
            } catch (_: Exception) {
                // Nếu AI respond lỗi thì vẫn giữ opening message + nhiệm vụ.
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

    private suspend fun sendAiRespond(userMessage: String) {
        val currentContext = _context.value
        val currentMessages = _uiState.value.messages

        val conversationHistory = buildConversationHistory(currentMessages)
        val taskDescription = currentContext.mission.joinToString(separator = "\n")

        val request = AiRespondRequest(
            scenarioDescription = currentContext.scenario,
            taskDescription = taskDescription,
            conversationHistory = conversationHistory,
            userMessage = userMessage
        )

        val result = aiRespondUseCase(request)

        val userBubble = Message(
            id = genId("u"),
            text = userMessage,
            translation = null,
            isFromAI = false,
            hint = null
        )

        val hint = Hint(
            analysis = result.userHints.analysis,
            suggestion = result.userHints.suggestion,
            example = result.userHints.example
        )

        val aiBubble = Message(
            id = genId("ai"),
            text = result.aiMessage,
            translation = result.aiMessageTranslation,
            isFromAI = true,
            hint = hint
        )

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userBubble + aiBubble,
                expandedHintMessageId = null
            )
        }
    }

    private fun buildConversationHistory(messages: List<Message>): String {
        return messages.joinToString(separator = "\n") { m ->
            if (m.isFromAI) "AI: ${m.text}" else "User: ${m.text}"
        }
    }

    private fun parseTasksToMissionList(tasks: String): List<String> {
        val normalized = tasks
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .trim()
        if (normalized.isBlank()) return emptyList()

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
}

