package com.nhom2.elearnlanguage.data.source.remote

import com.nhom2.elearnlanguage.BuildConfig
import com.nhom2.elearnlanguage.data.dto.ApiResponseDTO
import com.nhom2.elearnlanguage.data.dto.DeviceTokenRequestDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class DeviceTokenDataSource @Inject constructor(
    private val client: HttpClient
) {

    suspend fun registerDeviceToken(request: DeviceTokenRequestDTO): ApiResponseDTO<Unit> {
        return client.post("${BuildConfig.API_BASE_URL}/api/users/me/device-tokens") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun unregisterDeviceToken(token: String): ApiResponseDTO<Unit> {
        return client.delete("${BuildConfig.API_BASE_URL}/api/users/me/device-tokens") {
            parameter("token", token)
        }.body()
    }
}
