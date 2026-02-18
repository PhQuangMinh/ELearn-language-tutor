package com.nhom2.elearnlanguage.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDTO (
    val email: String,
    val username: String,
    val password: String,
    val fullName: String
)

@Serializable
data class LoginRequestDTO (
    val email: String,
    val password: String
)

@Serializable
data class GoogleLoginRequestDTO (
    val idToken: String
)

@Serializable
data class ForgotPasswordRequestDTO (
    val email: String
)

@Serializable
data class VerifyForgotPasswordCodeRequestDTO (
    val email: String,
    val code: String
)

@Serializable
data class ResetPasswordWithTokenRequestDTO (
    val email: String,
    val resetToken: String,
    val newPassword: String,
    val confirmPassword: String
)

@Serializable
data class RefreshTokenRequestDTO (
    val refreshToken: String
)