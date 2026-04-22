package com.nhom2.elearnlanguage.presentation.ui.main_app.lessonlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhom2.elearnlanguage.domain.usecase.GetLessonsByTopicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonListViewModel @Inject constructor(
    private val getLessonsByTopicUseCase: GetLessonsByTopicUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LessonListUiState>(LessonListUiState.Idle)
    val uiState: StateFlow<LessonListUiState> = _uiState.asStateFlow()

    private var loadedTopicId: Int? = null

    fun load(topicId: Int, topicName: String, topicImageUrl: String?, forceRefresh: Boolean = false) {
        if (!forceRefresh && loadedTopicId == topicId && _uiState.value is LessonListUiState.Success) {
            return
        }

        viewModelScope.launch {
            _uiState.value = LessonListUiState.Loading
            try {
                val lessons = getLessonsByTopicUseCase(topicId)
                val items = lessons.map { lesson ->
                    LessonRowUiModel(
                        id = lesson.id,
                        title = lesson.name,
                        completed = lesson.completed,
                        imageUrl = lesson.imageUrl
                    )
                }
                loadedTopicId = topicId
                _uiState.value = LessonListUiState.Success(
                    topicName = topicName,
                    topicImageUrl = topicImageUrl,
                    lessons = items
                )
            } catch (e: Exception) {
                loadedTopicId = null
                _uiState.value = LessonListUiState.Error(e.message)
            }
        }
    }
}

sealed class LessonListUiState {
    data object Idle : LessonListUiState()
    data object Loading : LessonListUiState()
    data class Success(
        val topicName: String,
        val topicImageUrl: String?,
        val lessons: List<LessonRowUiModel>
    ) : LessonListUiState()

    data class Error(val message: String?) : LessonListUiState()
}

data class LessonRowUiModel(
    val id: Int,
    val title: String,
    val completed: Boolean,
    val imageUrl: String?
)

