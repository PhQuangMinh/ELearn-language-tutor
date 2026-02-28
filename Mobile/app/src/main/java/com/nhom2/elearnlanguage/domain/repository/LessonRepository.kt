package com.nhom2.elearnlanguage.domain.repository

import com.nhom2.elearnlanguage.domain.model.LessonInTopic

interface LessonRepository {
    suspend fun getLessonsByTopic(topicId: Int): List<LessonInTopic>
}

