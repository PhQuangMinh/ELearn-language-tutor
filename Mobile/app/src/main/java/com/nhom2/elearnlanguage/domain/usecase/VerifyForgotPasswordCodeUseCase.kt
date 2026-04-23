package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyForgotPasswordCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, code: String): String {
        return authRepository.verifyForgotPasswordCode(email, code)
    }
}
