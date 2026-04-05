package com.nhom2.elearnlanguage.data.source.remote

import com.nhom2.elearnlanguage.BuildConfig
import com.nhom2.elearnlanguage.data.dto.ApiResponseDTO
import com.nhom2.elearnlanguage.data.dto.LessonInTopicDTO
import com.nhom2.elearnlanguage.data.dto.lesson.LessonSubmitRequest
import com.nhom2.elearnlanguage.data.dto.lesson.LessonSubmitResultDTO
import com.nhom2.elearnlanguage.data.dto.lesson.QuestionDetailDTO
import com.nhom2.elearnlanguage.data.dto.voice.VoiceAssessmentDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Headers
import io.ktor.http.contentType
import java.io.File
import javax.inject.Inject

class LessonDataSource @Inject constructor(
    private val client: HttpClient
) {

    suspend fun getLessonsByTopic(topicId: Int): ApiResponseDTO<List<LessonInTopicDTO>> {
        return client.get("${BuildConfig.API_BASE_URL}/api/topics/$topicId/lessons").body()
    }

    suspend fun getLessonsQuestions (lessonId: Int): List<QuestionDetailDTO> {
        return client.get("${BuildConfig.API_BASE_URL}/api/lessons/$lessonId/questions").body()
    }

    suspend fun submitLessonAnswers(lessonId: Int, request: LessonSubmitRequest): LessonSubmitResultDTO {
        return client.post("${BuildConfig.API_BASE_URL}/api/lessons/$lessonId/submit") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun assessSpeaking(referenceText: String, audioFile: File, language: String): VoiceAssessmentDTO {
        val response = client.submitFormWithBinaryData(
            url = "${BuildConfig.API_BASE_URL}/api/voice/assess",
            formData = formData {
                append("referenceText", referenceText)
                append("language", language)
                append(
                    key = "audio",
                    value = audioFile.readBytes(),
                    headers = Headers.build {
                        append(
                            HttpHeaders.ContentDisposition,
                            "form-data; name=\"audio\"; filename=\"${audioFile.name}\""
                        )
                        append(HttpHeaders.ContentType, "audio/wav")
                    }
                )
            }
        ).body<ApiResponseDTO<VoiceAssessmentDTO>>()

        if (!response.success || response.data == null) {
            throw IllegalStateException(response.message)
        }
        return response.data
    }
}
