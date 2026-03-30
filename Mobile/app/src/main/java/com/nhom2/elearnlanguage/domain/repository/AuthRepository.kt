package com.nhom2.elearnlanguage.domain.repository

import com.nhom2.elearnlanguage.data.dto.ApiResponseDTO
import com.nhom2.elearnlanguage.data.dto.AuthResponseDTO
import com.nhom2.elearnlanguage.data.dto.LoginRequestDTO
import com.nhom2.elearnlanguage.domain.model.AuthSession

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthSession
    suspend fun googleLogin(idToken: String): AuthSession
    suspend fun registerInitiate(fullName: String, email: String)
    suspend fun registerVerifyEmail(email: String, code: String): String
    suspend fun registerComplete(email: String, registerToken: String, password: String): AuthSession
    suspend fun forgotPassword(email: String)
    suspend fun verifyForgotPasswordCode(email: String, code: String): String
    suspend fun resetPassword(
        email: String,
        resetToken: String,
        newPassword: String,
        confirmPassword: String
    )

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    )
}