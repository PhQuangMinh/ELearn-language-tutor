package com.nhom2.elearnlanguage.data.source.remote

import com.nhom2.elearnlanguage.BuildConfig
import com.nhom2.elearnlanguage.data.dto.ApiResponseDTO
import com.nhom2.elearnlanguage.data.dto.AuthResponseDTO
import com.nhom2.elearnlanguage.data.dto.ForgotPasswordRequestDTO
import com.nhom2.elearnlanguage.data.dto.GoogleLoginRequestDTO
import com.nhom2.elearnlanguage.data.dto.LoginRequestDTO
import com.nhom2.elearnlanguage.data.dto.RegisterCompleteRequestDTO
import com.nhom2.elearnlanguage.data.dto.RegisterInitiateRequestDTO
import com.nhom2.elearnlanguage.data.dto.RegisterTokenResponseDTO
import com.nhom2.elearnlanguage.data.dto.ChangePasswordRequestDTO
import com.nhom2.elearnlanguage.data.dto.VerifyEmailRequestDTO
import com.nhom2.elearnlanguage.data.dto.ResetPasswordTokenResponseDTO
import com.nhom2.elearnlanguage.data.dto.ResetPasswordWithTokenRequestDTO
import com.nhom2.elearnlanguage.data.dto.VerifyForgotPasswordCodeRequestDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class AuthDataSource @Inject constructor(
    private val httpClient: HttpClient
) {
    suspend fun login(request: LoginRequestDTO): ApiResponseDTO<AuthResponseDTO> {
        return httpClient.post("${BuildConfig.API_BASE_URL}/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun registerInitiate(request: RegisterInitiateRequestDTO): ApiResponseDTO<Unit> {
        return httpClient.post("${BuildConfig.API_BASE_URL}/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun registerVerifyEmail(request: VerifyEmailRequestDTO): ApiResponseDTO<RegisterTokenResponseDTO> {
        return httpClient.post("${BuildConfig.API_BASE_URL}/api/auth/register/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun registerComplete(request: RegisterCompleteRequestDTO): ApiResponseDTO<AuthResponseDTO> {
        return httpClient.post("${BuildConfig.API_BASE_URL}/api/auth/register/complete") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun googleLogin(request: GoogleLoginRequestDTO): ApiResponseDTO<AuthResponseDTO> {
        return httpClient.post("${BuildConfig.API_BASE_URL}/api/auth/oauth2/google") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun forgotPassword(request: ForgotPasswordRequestDTO): ApiResponseDTO<Unit> {
        return httpClient.post("${BuildConfig.API_BASE_URL}/api/auth/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun verifyForgotPasswordCode(
        request: VerifyForgotPasswordCodeRequestDTO
    ): ApiResponseDTO<ResetPasswordTokenResponseDTO> {
        return httpClient.post("${BuildConfig.API_BASE_URL}/api/auth/forgot-password/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun resetPassword(request: ResetPasswordWithTokenRequestDTO): ApiResponseDTO<Unit> {
        return httpClient.post("${BuildConfig.API_BASE_URL}/api/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun changePassword(request: ChangePasswordRequestDTO): ApiResponseDTO<Unit> {
        return httpClient.post("${BuildConfig.API_BASE_URL}/api/auth/change-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}