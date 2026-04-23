package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
){
    suspend operator fun invoke(
        email: String,
        resetToken: String,
        newPassword: String,
        confirmPassword: String
    ) {
        authRepository.resetPassword(email, resetToken, newPassword, confirmPassword)
    }
}