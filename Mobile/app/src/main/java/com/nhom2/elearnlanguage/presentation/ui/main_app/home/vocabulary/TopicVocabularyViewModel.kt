package com.nhom2.elearnlanguage.presentation.ui.main_app.home.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhom2.elearnlanguage.domain.model.vocabulary.Vocabulary
import com.nhom2.elearnlanguage.domain.model.vocabulary.VocabularyType
import com.nhom2.elearnlanguage.domain.usecase.GetTopicVocabulariesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

@HiltViewModel
class TopicVocabularyViewModel @Inject constructor(
    private val getTopicVocabulariesUseCase: GetTopicVocabulariesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TopicVocabularyUiState>(TopicVocabularyUiState.Idle)
    val uiState: StateFlow<TopicVocabularyUiState> = _uiState.asStateFlow()

    private var topicTitle: String = "Travelling"
    private var selectedType: VocabularyType = VocabularyType.NOUN
    private var currentPage: Int = 1
    private var all: List<Vocabulary> = emptyList()
    private var loadedTopicId: Int? = null

    fun load(topicId: Int, topicTitle: String) {
        if (loadedTopicId == topicId && _uiState.value is TopicVocabularyUiState.Success) {
            return
        }

        this.topicTitle = topicTitle
        if (loadedTopicId != topicId) {
            selectedType = VocabularyType.NOUN
            currentPage = 1
        }
        viewModelScope.launch {
            _uiState.value = TopicVocabularyUiState.Loading
            try {
                all = getTopicVocabulariesUseCase(topicId)
                loadedTopicId = topicId
                emitSuccess()
            } catch (e: Exception) {
                loadedTopicId = null
                _uiState.value = TopicVocabularyUiState.Error(e.message)
            }
        }
    }

    fun selectType(type: VocabularyType) {
        if (selectedType == type) return
        selectedType = type
        currentPage = 1
        emitSuccess()
    }

    fun selectPage(page: Int) {
        val maxPage = calculateTotalPages()
        val nextPage = min(max(1, page), maxPage)
        if (currentPage == nextPage) return
        currentPage = nextPage
        emitSuccess()
    }

    private fun emitSuccess() {
        val filtered = all.filter { it.type == selectedType }
        val pageSize = PAGE_SIZE
        val totalPages = calculateTotalPages(filtered.size, pageSize)
        val safePage = min(max(1, currentPage), max(1, totalPages))
        currentPage = safePage

        val startIndex = (safePage - 1) * pageSize
        val endIndex = min(startIndex + pageSize, filtered.size)
        val pageItems = if (filtered.isEmpty() || startIndex >= filtered.size) {
            emptyList()
        } else {
            filtered.subList(startIndex, endIndex)
        }

        _uiState.value = TopicVocabularyUiState.Success(
            topicTitle = topicTitle,
            selectedType = selectedType,
            currentPage = safePage,
            totalPages = totalPages,
            pages = (1..max(1, totalPages)).toList(),
            items = pageItems.map { it.toUi() }
        )
    }

    private fun calculateTotalPages(): Int = calculateTotalPages(
        filteredSize = all.count { it.type == selectedType },
        pageSize = PAGE_SIZE
    )

    private fun calculateTotalPages(filteredSize: Int, pageSize: Int): Int {
        // Keep pagination stable even when there are 0 items (UI can show 1/1 with empty list).
        if (filteredSize <= 0) return 1
        return ceil(filteredSize / pageSize.toDouble()).toInt()
    }

    private fun Vocabulary.toUi(): VocabularyCardUiModel = VocabularyCardUiModel(
        word = word,
        meaning = meaning,
        pronunciation = pronunciation,
        definition = definition,
        example = example
    )

    private companion object {
        const val PAGE_SIZE = 4
    }
}

sealed class TopicVocabularyUiState {
    data object Idle : TopicVocabularyUiState()
    data object Loading : TopicVocabularyUiState()

    data class Success(
        val topicTitle: String,
        val selectedType: VocabularyType,
        val currentPage: Int,
        val totalPages: Int,
        val pages: List<Int>,
        val items: List<VocabularyCardUiModel>
    ) : TopicVocabularyUiState()

    data class Error(val message: String?) : TopicVocabularyUiState()
}

