package com.nhom2.elearnlanguage.data.source.remote

import com.nhom2.elearnlanguage.BuildConfig
import com.nhom2.elearnlanguage.data.dto.ApiResponseDTO
import com.nhom2.elearnlanguage.data.dto.CourseProgressDTO
import com.nhom2.elearnlanguage.data.dto.HomeDataDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

class HomeDataSource @Inject constructor(private val client: HttpClient) {

    suspend fun getHomeData(): ApiResponseDTO<HomeDataDTO> {
        return client.get("${BuildConfig.API_BASE_URL}/api/home").body()
    }

    suspend fun getCourses(page: Int, size: Int): ApiResponseDTO<List<CourseProgressDTO>> {
        return client.get("${BuildConfig.API_BASE_URL}/api/home/courses") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())
            }
        }.body()
    }
}
