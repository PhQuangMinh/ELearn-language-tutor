package com.nhom2.elearnlanguage.domain.model

data class AuthSession(
    val accessToken: String,
    val refreshToken: String?,
    val user: User?
)
