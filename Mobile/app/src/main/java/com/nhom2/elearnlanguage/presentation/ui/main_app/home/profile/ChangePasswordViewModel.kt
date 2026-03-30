package com.nhom2.elearnlanguage.presentation.ui.main_app.home.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhom2.elearnlanguage.domain.usecase.ChangePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChangePasswordUiState {
    data object Idle : ChangePasswordUiState()
    data object Loading : ChangePasswordUiState()
    data object Success : ChangePasswordUiState()
    data class Error(val message: String) : ChangePasswordUiState()
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChangePasswordUiState>(ChangePasswordUiState.Idle)
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _uiState.value = ChangePasswordUiState.Loading
            try {
                changePasswordUseCase(currentPassword, newPassword, confirmPassword)
                _uiState.value = ChangePasswordUiState.Success
            } catch (e: Exception) {
                _uiState.value = ChangePasswordUiState.Error(e.message ?: "Change password failed.")
            }
        }
    }

    fun resetState() {
        _uiState.value = ChangePasswordUiState.Idle
    }
}
