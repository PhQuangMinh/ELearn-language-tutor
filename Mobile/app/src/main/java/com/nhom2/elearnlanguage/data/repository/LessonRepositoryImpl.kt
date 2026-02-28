package com.nhom2.elearnlanguage.data.repository

import com.nhom2.elearnlanguage.data.mapper.LessonMapper
import com.nhom2.elearnlanguage.data.source.remote.LessonDataSource
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
}

