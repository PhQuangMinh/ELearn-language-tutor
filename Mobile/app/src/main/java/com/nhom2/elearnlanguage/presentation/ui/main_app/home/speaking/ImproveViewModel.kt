package com.nhom2.elearnlanguage.presentation.ui.main_app.home.speaking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhom2.elearnlanguage.R
import com.nhom2.elearnlanguage.domain.usecase.ImproveMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImproveViewModel @Inject constructor(
    private val improveMessageUseCase: ImproveMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImproveUiState())
    val uiState: StateFlow<ImproveUiState> = _uiState.asStateFlow()

    fun loadImprovement(originalTextInput: String) {
        val currentState = _uiState.value
        val originalText = originalTextInput.trim()

        if (originalText.isBlank()) {
            _uiState.value = currentState.copy(
                originalText = "",
                errorMessageRes = R.string.improve_error_empty_input
            )
            return
        }

        if (currentState.originalText == originalText &&
            currentState.improvedText.isNotBlank() &&
            currentState.explanation.isNotBlank()
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                originalText = originalText,
                isLoading = true,
                improvedText = "",
                explanation = "",
                errorMessageRes = null
            )

            val improveResult = runCatching {
                improveMessageUseCase(
                    text = originalText,
                    context = DEFAULT_IMPROVE_CONTEXT
                )
            }.getOrNull()

            if (improveResult == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessageRes = R.string.improve_error_generic
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                originalText = improveResult.original,
                improvedText = improveResult.improved,
                explanation = improveResult.explanation,
                errorMessageRes = null
            )
        }
    }
}

private const val DEFAULT_IMPROVE_CONTEXT = "General English conversation practice."

data class ImproveUiState(
    val originalText: String = "",
    val isLoading: Boolean = false,
    val improvedText: String = "",
    val explanation: String = "",
    val errorMessageRes: Int? = null
)
