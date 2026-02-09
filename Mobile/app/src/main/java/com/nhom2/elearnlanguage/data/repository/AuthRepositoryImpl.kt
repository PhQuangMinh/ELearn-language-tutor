package com.nhom2.elearnlanguage.data.repository

import android.util.Log
import com.nhom2.elearnlanguage.data.dto.ApiResponseDTO
import com.nhom2.elearnlanguage.data.dto.AuthResponseDTO
import com.nhom2.elearnlanguage.data.dto.LoginRequestDTO
import com.nhom2.elearnlanguage.data.dto.RegisterRequestDTO
import com.nhom2.elearnlanguage.data.mapper.AuthMapper
import com.nhom2.elearnlanguage.data.source.remote.AuthDataSource
import com.nhom2.elearnlanguage.domain.model.AuthSession
import com.nhom2.elearnlanguage.domain.repository.AuthRepository
import io.ktor.client.plugins.logging.Logging
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val authDataSource: AuthDataSource) :
    AuthRepository {
    override suspend fun login(username: String, password: String): AuthSession {
        val response = authDataSource.login(LoginRequestDTO(username, password))

        if (!response.success || response.data == null) {
            throw Exception(response.errorCode ?: response.message)
        }

        Log.d("LOGIN", response.data.toString())
        return AuthMapper.toAuthSession(response.data)
    }


    override suspend fun register(
        username: String,
        email: String,
        password: String,
        fullName: String?,
        phoneNumber: String?
    ): AuthSession {
        val response = authDataSource.register(
            RegisterRequestDTO(
                email = email,
                username = username,
                password = password,
                fullName = fullName ?: ""
            )
        )

        if (!response.success || response.data == null) {
            throw Exception(response.errorCode ?: response.message)
        }

        return AuthMapper.toAuthSession(response.data)
    }
}
