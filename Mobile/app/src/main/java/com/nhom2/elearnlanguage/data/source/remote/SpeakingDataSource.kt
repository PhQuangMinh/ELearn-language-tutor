package com.nhom2.elearnlanguage.data.source.remote

import com.nhom2.elearnlanguage.BuildConfig
import com.nhom2.elearnlanguage.data.dto.AiRespondRequestDTO
import com.nhom2.elearnlanguage.data.dto.AiRespondResponseDTO
import com.nhom2.elearnlanguage.data.dto.ApiResponseDTO
import com.nhom2.elearnlanguage.data.dto.ScenarioDetailResponseDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class SpeakingDataSource @Inject constructor(
    private val httpClient: HttpClient
) {
    suspend fun getScenarioByLesson(lessonId: Int): ApiResponseDTO<ScenarioDetailResponseDTO> {
        return httpClient.get("${BuildConfig.API_BASE_URL}/api/scenarios/by-lesson/$lessonId").body()
    }

    suspend fun aiRespond(request: AiRespondRequestDTO): ApiResponseDTO<AiRespondResponseDTO> {
        return httpClient.post("${BuildConfig.API_BASE_URL}/api/ai/respond") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

