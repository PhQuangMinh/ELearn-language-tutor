package com.nhom2.elearnlanguage.data.source.remote

import com.nhom2.elearnlanguage.BuildConfig
import com.nhom2.elearnlanguage.data.dto.lesson.QuestionDetailDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

class LessonDataSource @Inject constructor(
    private val client: HttpClient
) {

    suspend fun getLessonsQuestions (lessonId: Int): List<QuestionDetailDTO> {
        return client.get("${BuildConfig.API_BASE_URL}/api/lessons/$lessonId/questions").body()
    }
}
