package com.nhom2.elearnlanguage.presentation.ui.main_app.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhom2.elearnlanguage.domain.model.lesson.Question
import com.nhom2.elearnlanguage.domain.model.lesson.LessonSubmitResult
import com.nhom2.elearnlanguage.domain.model.lesson.SpeakingAssessmentResult
import com.nhom2.elearnlanguage.data.dto.lesson.LessonSubmitRequest
import com.nhom2.elearnlanguage.domain.usecase.AssessSpeakingAssessmentUseCase
import com.nhom2.elearnlanguage.domain.usecase.GetLessonQuestionsUseCase
import com.nhom2.elearnlanguage.domain.usecase.SubmitLessonAnswersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LessonViewModel @Inject constructor(
    private val getLessonQuestionsUseCase: GetLessonQuestionsUseCase,
    private val submitLessonAnswersUseCase: SubmitLessonAnswersUseCase,
    private val assessSpeakingAssessmentUseCase: AssessSpeakingAssessmentUseCase
): ViewModel() {

    private val _questionsState = MutableStateFlow<LessonQuestionsState>(LessonQuestionsState.Initial)
    val questionsState: StateFlow<LessonQuestionsState> = _questionsState.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private var allQuestions: List<Question> = emptyList()
    private var currentLessonId: Int? = null

    fun loadLessonQuestions(lessonId: Int) {
        if (currentLessonId == lessonId && _questionsState.value is LessonQuestionsState.Success) {
            return
        }

        viewModelScope.launch {
            currentLessonId = lessonId
            allQuestions = emptyList()
            _currentQuestionIndex.value = 0
            _questionsState.value = LessonQuestionsState.Loading

            getLessonQuestionsUseCase(lessonId).fold(
                onSuccess = { questions ->
                    allQuestions = questions
                    _questionsState.value = LessonQuestionsState.Success(questions)
                    _currentQuestionIndex.value = 0
                },
                onFailure = { error ->
                    _questionsState.value = LessonQuestionsState.Error(
                        error.message ?: "Failed to load questions"
                    )
                }
            )
        }
    }

    fun getCurrentQuestion(): Question? {
        return allQuestions.getOrNull(_currentQuestionIndex.value)
    }

    fun getNextQuestion(): Question? {
        return allQuestions.getOrNull(_currentQuestionIndex.value + 1)
    }

    fun moveToNextQuestion() {
        if (_currentQuestionIndex.value < allQuestions.size - 1) {
            _currentQuestionIndex.value += 1
        }
    }

    fun moveToPreviousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }

    fun shouldSkipAutoRoute(): Boolean {
        return false  // Always allow routing initially
    }

    fun isLastQuestion(): Boolean {
        return _currentQuestionIndex.value == allQuestions.size - 1
    }

    fun getTotalQuestions(): Int = allQuestions.size

    /**
     * Get current progress (number of questions completed + current question)
     * For progress bar display
     */
    fun getProgressCount(): Int = _currentQuestionIndex.value + 1

    suspend fun submitLessonAnswers(lessonId: Int, request: LessonSubmitRequest): Result<LessonSubmitResult> {
        return submitLessonAnswersUseCase(lessonId, request)
    }

    suspend fun assessSpeakingAssessment(
        referenceText: String,
        audioFile: File,
        language: String = "US_ENGLISH"
    ): Result<SpeakingAssessmentResult> {
        return assessSpeakingAssessmentUseCase(referenceText, audioFile, language)
    }
}

sealed class LessonQuestionsState {
    object Initial : LessonQuestionsState()
    object Loading : LessonQuestionsState()
    data class Success(val questions: List<Question>) : LessonQuestionsState()
    data class Error(val message: String) : LessonQuestionsState()
}