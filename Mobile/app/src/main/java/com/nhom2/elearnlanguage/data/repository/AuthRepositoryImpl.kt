package com.nhom2.elearnlanguage.data.repository

import android.util.Log
import com.nhom2.elearnlanguage.data.dto.ApiResponseDTO
import com.nhom2.elearnlanguage.data.dto.AuthResponseDTO
import com.nhom2.elearnlanguage.data.dto.ForgotPasswordRequestDTO
import com.nhom2.elearnlanguage.data.dto.GoogleLoginRequestDTO
import com.nhom2.elearnlanguage.data.dto.LoginRequestDTO
import com.nhom2.elearnlanguage.data.dto.RegisterRequestDTO
import com.nhom2.elearnlanguage.data.dto.ResetPasswordWithTokenRequestDTO
import com.nhom2.elearnlanguage.data.dto.VerifyForgotPasswordCodeRequestDTO
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
            throw Exception(response.message)
        }

        Log.d("LOGIN", response.data.toString())
        return AuthMapper.toAuthSession(response.data)
    }

    override suspend fun googleLogin(idToken: String): AuthSession {
        val response = authDataSource.googleLogin(GoogleLoginRequestDTO(idToken))

        if (!response.success || response.data == null) {
            Log.d("GOOGLE LOGIN", response.toString())
            throw Exception(response.message)
        }

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
            throw Exception(response.message)
        }

        return AuthMapper.toAuthSession(response.data)
    }

    override suspend fun forgotPassword(email: String) {
        val request = ForgotPasswordRequestDTO(email)
        val response = authDataSource.forgotPassword(request)

        if (!response.success) {
            Log.d("FORGOT PASSWORD", response.toString())
            throw Exception(response.message)
        }
    }

    override suspend fun verifyForgotPasswordCode(email: String, code: String): String {
        val request = VerifyForgotPasswordCodeRequestDTO(email, code)
        val response = authDataSource.verifyForgotPasswordCode(request)

        if (!response.success || response.data == null) {
            Log.d("VERIFY FORGOT CODE", response.toString())
            throw Exception(response.message)
        }

        return response.data.resetToken
    }

    override suspend fun resetPassword(
        email: String,
        resetToken: String,
        newPassword: String,
        confirmPassword: String
    ) {
        val request = ResetPasswordWithTokenRequestDTO(
            email = email,
            resetToken = resetToken,
            newPassword = newPassword,
            confirmPassword = confirmPassword
        )
        val response = authDataSource.resetPassword(request)

        if (!response.success) {
            Log.d("RESET PASSWORD", response.toString())
            throw Exception(response.message)
        }
    }
}
