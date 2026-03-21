package com.nhom2.elearnlanguage.presentation.ui.main_app.home.speaking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.nhom2.elearnlanguage.domain.model.speaking.LessonScenario
import com.nhom2.elearnlanguage.domain.usecase.GetScenarioByLessonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AiConversationViewModel @Inject constructor(
    private val getScenarioByLessonUseCase: GetScenarioByLessonUseCase,
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

            applyScenario(scenario)
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
    fun addUserMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

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

