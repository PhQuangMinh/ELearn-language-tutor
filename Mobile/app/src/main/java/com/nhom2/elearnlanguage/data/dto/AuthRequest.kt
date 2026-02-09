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
    val username: String,
    val password: String
)

@Serializable
data class RefreshTokenRequestDTO (
    val refreshToken: String
)