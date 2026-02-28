package com.nhom2.elearnlanguage.data.source.remote

import com.nhom2.elearnlanguage.BuildConfig
import com.nhom2.elearnlanguage.data.dto.ApiResponseDTO
import com.nhom2.elearnlanguage.data.dto.LessonInTopicDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

class LessonDataSource @Inject constructor(
    private val httpClient: HttpClient
) {
    suspend fun getLessonsByTopic(topicId: Int): ApiResponseDTO<List<LessonInTopicDTO>> {
        return httpClient.get("${BuildConfig.API_BASE_URL}/api/topics/$topicId/lessons").body()
    }
}

