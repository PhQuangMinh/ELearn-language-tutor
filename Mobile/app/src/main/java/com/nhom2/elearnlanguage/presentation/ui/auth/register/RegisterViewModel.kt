package com.nhom2.elearnlanguage.presentation.ui.auth.register

import androidx.lifecycle.ViewModel
import com.nhom2.elearnlanguage.domain.repository.AuthRepository
import com.nhom2.elearnlanguage.domain.repository.TokenStorage
import com.nhom2.elearnlanguage.domain.usecase.SyncFcmTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel // -> service/component
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage,
    private val syncFcmTokenUseCase: SyncFcmTokenUseCase
) : ViewModel() {//Inject constructor() -> autowired
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow() // Biến thể hiện để cập nhật trên giao diện, không trực tiếp sửa đổi

    private val _email = MutableStateFlow("") // private field
    val email = _email.asStateFlow() // getEmail()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _registerToken = MutableStateFlow("")
    val registerToken = _registerToken.asStateFlow()

    fun setName(value: String){
        _name.value = value
    }

    fun setEmail(value: String){
        _email.value = value
    }

    fun setPassword(value: String){
        _password.value = value
    }

    fun clearRegisterToken() {
        _registerToken.value = ""
    }

    suspend fun initiateRegister(): Unit {
        val fullName = name.value.trim()
        val email = email.value.trim()
        authRepository.registerInitiate(fullName = fullName, email = email)
    }

    suspend fun verifyEmailCode(code: String): Unit {
        val email = email.value.trim()
        val token = authRepository.registerVerifyEmail(email = email, code = code.trim())
        _registerToken.value = token
    }

    suspend fun completeRegister(password: String) {
        val email = email.value.trim()
        val token = registerToken.value.trim()
        val session = authRepository.registerComplete(
            email = email,
            registerToken = token,
            password = password.trim()
        )
        tokenStorage.saveAccessToken(session.accessToken)
        tokenStorage.saveRefreshToken(session.refreshToken)
        tokenStorage.saveUserId(session.user?.id)
        syncFcmTokenUseCase()
    }
}