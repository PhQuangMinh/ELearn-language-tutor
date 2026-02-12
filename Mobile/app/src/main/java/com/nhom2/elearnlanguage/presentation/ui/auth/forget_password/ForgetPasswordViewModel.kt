package com.nhom2.elearnlanguage.presentation.ui.auth.forget_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhom2.elearnlanguage.domain.usecase.ForgotPasswordUseCase
import com.nhom2.elearnlanguage.domain.usecase.ResetPasswordUseCase
import com.nhom2.elearnlanguage.domain.usecase.VerifyForgotPasswordCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgetPasswordViewModel @Inject constructor(
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val verifyForgotPasswordCodeUseCase: VerifyForgotPasswordCodeUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow<ResetPasswordUIState>(ResetPasswordUIState.Idle)
    val uiState: StateFlow<ResetPasswordUIState> = _uiState.asStateFlow()

    private val _email = MutableStateFlow<String>("")
    val email = _email.asStateFlow()

    private val _error = MutableStateFlow<String>("")
    val error = _error.asStateFlow()

    private val _resetToken = MutableStateFlow<String>("")
    val resetToken = _resetToken.asStateFlow()

    fun resetPassword(email: String, resetToken: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            setError("")
            _uiState.value = ResetPasswordUIState.Loading
            try {
                resetPasswordUseCase(email, resetToken, newPassword, confirmPassword)
                _uiState.value = ResetPasswordUIState.Success
            } catch (e: Exception) {
                setError(e.message.orEmpty())
                _uiState.value = ResetPasswordUIState.Error(e.message)
            }
        }
    }

    fun setEmail(email: String) {
        _email.value = email
    }

    fun setResetToken(resetToken: String) {
        _resetToken.value = resetToken
    }

    fun setError(error: String) {
        _error.value = error
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            setError("")
            _uiState.value = ResetPasswordUIState.Loading
            try {
                forgotPasswordUseCase(email)
                _uiState.value = ResetPasswordUIState.Success
            } catch (e: Exception) {
                setError(e.message.orEmpty())
                _uiState.value = ResetPasswordUIState.Error(e.message)
            }
        }
    }

    fun verifyForgotPasswordCode(email: String, code: String) {
        viewModelScope.launch {
            setError("")
            _uiState.value = ResetPasswordUIState.Loading
            try {
                val token = verifyForgotPasswordCodeUseCase(email, code)
                _resetToken.value = token
                _uiState.value = ResetPasswordUIState.Success
            } catch (e: Exception) {
                setError(e.message.orEmpty())
                _uiState.value = ResetPasswordUIState.Error(e.message)
            }
        }
    }

    fun resetState() {
        _uiState.value = ResetPasswordUIState.Idle
    }
}

sealed class ResetPasswordUIState {
    data object Idle : ResetPasswordUIState()
    data object Loading : ResetPasswordUIState()
    data object Success : ResetPasswordUIState()
    data class Error(val message: String?) : ResetPasswordUIState()
}

