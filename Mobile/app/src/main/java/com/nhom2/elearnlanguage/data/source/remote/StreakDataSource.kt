package com.nhom2.elearnlanguage.data.source.remote

import com.nhom2.elearnlanguage.BuildConfig
import com.nhom2.elearnlanguage.data.dto.ApiResponseDTO
import com.nhom2.elearnlanguage.data.dto.UserStreakDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

class StreakDataSource @Inject constructor(private val client: HttpClient) {
    suspend fun getUserStreak(userId: Long): ApiResponseDTO<UserStreakDTO> {
        return client.get("${BuildConfig.API_BASE_URL}/api/streak/user/$userId").body()
    }
}
