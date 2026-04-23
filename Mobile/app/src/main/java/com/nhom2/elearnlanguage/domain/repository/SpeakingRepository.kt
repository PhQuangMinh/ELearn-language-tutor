package com.nhom2.elearnlanguage.domain.repository

import com.nhom2.elearnlanguage.domain.model.speaking.AiRespondRequest
import com.nhom2.elearnlanguage.domain.model.speaking.AiRespondResult
import com.nhom2.elearnlanguage.domain.model.speaking.LessonScenario

interface SpeakingRepository {
    suspend fun getScenarioByLesson(lessonId: Int): LessonScenario
    suspend fun initSpeakingSession(scenarioId: Int): Int
    suspend fun endSpeakingSession(sessionId: Int)
    suspend fun aiRespond(request: AiRespondRequest): AiRespondResult
}

