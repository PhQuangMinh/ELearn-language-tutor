package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.repository.AuthRepository
import javax.inject.Inject

class ForgotPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
){
    suspend operator fun invoke(email: String) {
        authRepository.forgotPassword(email)
    }
}