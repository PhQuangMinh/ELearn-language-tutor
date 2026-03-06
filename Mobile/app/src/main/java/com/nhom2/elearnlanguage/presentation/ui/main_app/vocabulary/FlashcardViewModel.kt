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

    fun getFlashcards(topicId: Int? = null) {
        viewModelScope.launch {
            val result = getFlashcardListOfTopicUseCase(topicId)
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
