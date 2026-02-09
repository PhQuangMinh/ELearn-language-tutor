package com.nhom2.elearnlanguage.domain.repository

import com.nhom2.elearnlanguage.data.dto.ApiResponseDTO
import com.nhom2.elearnlanguage.data.dto.AuthResponseDTO
import com.nhom2.elearnlanguage.data.dto.LoginRequestDTO
import com.nhom2.elearnlanguage.data.dto.RegisterRequestDTO
import com.nhom2.elearnlanguage.domain.model.AuthSession

interface AuthRepository {
    suspend fun login(username: String, password: String): AuthSession
    suspend fun register(
        username: String,
        email: String,
        password: String,
        fullName: String?,
        phoneNumber: String?
    ): AuthSession
}