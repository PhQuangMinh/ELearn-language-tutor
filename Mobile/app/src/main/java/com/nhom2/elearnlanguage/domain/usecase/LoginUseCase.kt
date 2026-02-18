package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.model.AuthSession
import com.nhom2.elearnlanguage.domain.repository.AuthRepository
import com.nhom2.elearnlanguage.domain.repository.TokenStorage
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage
) {
    suspend operator fun invoke(email: String, password: String): AuthSession {
        val session = authRepository.login(email, password)
        tokenStorage.saveAccessToken(session.accessToken)
        tokenStorage.saveRefreshToken(session.refreshToken)
        return session
    }
}