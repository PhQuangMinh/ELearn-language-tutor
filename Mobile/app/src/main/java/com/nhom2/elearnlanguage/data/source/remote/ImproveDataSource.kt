package com.nhom2.elearnlanguage.data.source.remote

import com.nhom2.elearnlanguage.BuildConfig
import com.nhom2.elearnlanguage.data.dto.ApiResponseDTO
import com.nhom2.elearnlanguage.data.dto.ImproveRequestDTO
import com.nhom2.elearnlanguage.data.dto.ImproveResponseDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class ImproveDataSource @Inject constructor(
    private val client: HttpClient
) {

    suspend fun improveMessage(message: String): ApiResponseDTO<ImproveResponseDTO> {
        return client.post("${BuildConfig.API_BASE_URL}/api/chat/improve") {
            contentType(ContentType.Application.Json)
            setBody(ImproveRequestDTO(message = message))
        }.body()
    }
}
