package com.nhom2.elearnlanguage.presentation.ui.main_app.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhom2.elearnlanguage.domain.model.Flashcard
import com.nhom2.elearnlanguage.domain.usecase.GetFlashcardListOfTopicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    private val getFlashcardListOfTopicUseCase: GetFlashcardListOfTopicUseCase
) : ViewModel() {

    private val _flashcards = MutableStateFlow<List<Flashcard>>(emptyList())
    val flashcards: StateFlow<List<Flashcard>> = _flashcards

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private var currentTopicId: Int? = null
    private val cacheByTopicId: MutableMap<Int, List<Flashcard>> = mutableMapOf()

    fun getFlashcards(topicId: Int? = null, forceRefresh: Boolean = false) {
        if (topicId == null) return

        // If we're opening the same topic again, reuse cached data to avoid reloading.
        if (!forceRefresh && currentTopicId == topicId) {
            _currentIndex.value = 0
            return
        }

        val cached = if (!forceRefresh) cacheByTopicId[topicId] else null
        if (cached != null) {
            currentTopicId = topicId
            _flashcards.value = cached
            _currentIndex.value = 0
            return
        }

        viewModelScope.launch {
            val result = getFlashcardListOfTopicUseCase(topicId)
            cacheByTopicId[topicId] = result
            currentTopicId = topicId
            _flashcards.value = result
            _currentIndex.value = 0
        }
    }

    fun setCurrentIndex(index: Int) {
        if (index >= 0 && index < _flashcards.value.size) {
            _currentIndex.value = index
        }
    }
}
