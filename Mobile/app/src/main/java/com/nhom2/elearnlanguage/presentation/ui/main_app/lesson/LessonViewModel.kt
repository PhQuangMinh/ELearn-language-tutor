package com.nhom2.elearnlanguage.presentation.ui.main_app.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhom2.elearnlanguage.domain.model.lesson.Question
import com.nhom2.elearnlanguage.domain.usecase.GetLessonQuestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonViewModel @Inject constructor(
    private val getLessonQuestionsUseCase: GetLessonQuestionsUseCase
): ViewModel() {

    private val _questionsState = MutableStateFlow<LessonQuestionsState>(LessonQuestionsState.Initial)
    val questionsState: StateFlow<LessonQuestionsState> = _questionsState.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private var allQuestions: List<Question> = emptyList()

    fun loadLessonQuestions(lessonId: Int) {
        viewModelScope.launch {
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
}

sealed class LessonQuestionsState {
    object Initial : LessonQuestionsState()
    object Loading : LessonQuestionsState()
    data class Success(val questions: List<Question>) : LessonQuestionsState()
    data class Error(val message: String) : LessonQuestionsState()
}