package com.nhom2.elearnlanguage.presentation.ui.main_app.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhom2.elearnlanguage.domain.usecase.ObserveSpeechUseCase
import com.nhom2.elearnlanguage.domain.usecase.StartListeningUseCase
import com.nhom2.elearnlanguage.domain.usecase.StopListeningUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val startListeningUseCase: StartListeningUseCase,
    private val stopListeningUseCase: StopListeningUseCase,
    private val observeSpeechUseCase: ObserveSpeechUseCase
): ViewModel() {
    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

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
}