package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.model.AuthSession
import com.nhom2.elearnlanguage.domain.repository.AuthRepository
import com.nhom2.elearnlanguage.domain.repository.TokenStorage
import javax.inject.Inject

class GoogleLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage
) {
    suspend operator fun invoke(idToken: String): AuthSession {
        val session = authRepository.googleLogin(idToken)
        tokenStorage.saveAccessToken(session.accessToken)
        tokenStorage.saveRefreshToken(session.refreshToken)
        tokenStorage.saveUserId(session.user?.id)
        return session
    }
}
