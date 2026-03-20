package com.nhom2.elearnlanguage.data.repository

import com.nhom2.elearnlanguage.data.mapper.toDomain
import com.nhom2.elearnlanguage.data.source.remote.SpeakingDataSource
import com.nhom2.elearnlanguage.domain.model.speaking.AiRespondRequest
import com.nhom2.elearnlanguage.domain.model.speaking.AiRespondResult
import com.nhom2.elearnlanguage.domain.model.speaking.LessonScenario
import com.nhom2.elearnlanguage.domain.repository.SpeakingRepository
import javax.inject.Inject

class SpeakingRepositoryImpl @Inject constructor(
    private val speakingDataSource: SpeakingDataSource
) : SpeakingRepository {

    override suspend fun getScenarioByLesson(lessonId: Int): LessonScenario {
        val response = speakingDataSource.getScenarioByLesson(lessonId)
        if (!response.success || response.data == null) {
            throw Exception(response.message)
        }
        return response.data.toDomain()
    }

    override suspend fun aiRespond(request: AiRespondRequest): AiRespondResult {
        val response = speakingDataSource.aiRespond(request.toDto())
        if (!response.success || response.data == null) {
            throw Exception(response.message)
        }
        return response.data.toDomain()
    }
}

private fun AiRespondRequest.toDto() = com.nhom2.elearnlanguage.data.dto.AiRespondRequestDTO(
    scenarioDescription = scenarioDescription,
    taskDescription = taskDescription,
    conversationHistory = conversationHistory,
    userMessage = userMessage
)

