package com.nhom2.elearnlanguage.data.repository

import android.util.Log
import com.nhom2.elearnlanguage.data.mapper.toDomain
import com.nhom2.elearnlanguage.data.mapper.LessonMapper
import com.nhom2.elearnlanguage.data.source.remote.LessonDataSource
import com.nhom2.elearnlanguage.data.dto.lesson.LessonSubmitRequest
import com.nhom2.elearnlanguage.domain.model.lesson.Question
import com.nhom2.elearnlanguage.domain.model.LessonInTopic
import com.nhom2.elearnlanguage.domain.repository.LessonRepository
import javax.inject.Inject

class LessonRepositoryImpl @Inject constructor(
    private val lessonDataSource: LessonDataSource
) : LessonRepository {
    override suspend fun getLessonsByTopic(topicId: Int): List<LessonInTopic> {
        val response = lessonDataSource.getLessonsByTopic(topicId)
        if (!response.success || response.data == null) {
            throw Exception(response.message)
        }
        return response.data.map(LessonMapper::toLessonInTopic)
    }

    override suspend fun getLessonQuestions(lessonId: Int): List<Question> {
        val response = lessonDataSource.getLessonsQuestions(lessonId)
        Log.d("LESSON_API", "getLessonQuestions success: $response")
        return response.map { it.toDomain() }
    }

    override suspend fun submitLessonAnswers(lessonId: Int, request: LessonSubmitRequest): Boolean {
        return lessonDataSource.submitLessonAnswers(lessonId, request)
    }
}

