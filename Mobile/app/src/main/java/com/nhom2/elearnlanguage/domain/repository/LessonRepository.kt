package com.nhom2.elearnlanguage.domain.repository

import com.nhom2.elearnlanguage.domain.model.lesson.Question
import com.nhom2.elearnlanguage.data.dto.lesson.LessonSubmitRequest
import com.nhom2.elearnlanguage.domain.model.LessonInTopic
import com.nhom2.elearnlanguage.domain.model.lesson.LessonSubmitResult
import com.nhom2.elearnlanguage.domain.model.lesson.SpeakingAssessmentResult
import java.io.File

interface LessonRepository {
    suspend fun getLessonsByTopic(topicId: Int): List<LessonInTopic>

    suspend fun getLessonQuestions(lessonId: Int): List<Question>

    suspend fun submitLessonAnswers(lessonId: Int, request: LessonSubmitRequest): LessonSubmitResult

    suspend fun assessSpeaking(referenceText: String, audioFile: File, language: String = "US_ENGLISH"): SpeakingAssessmentResult
}
