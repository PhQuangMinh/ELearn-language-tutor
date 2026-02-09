package com.nhom2.elearnlanguage.data.dto

data class AuthResponseDTO (
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String,
    val role: String,
    val token: String,
    val refreshToken: String?,
    val type: String?
)