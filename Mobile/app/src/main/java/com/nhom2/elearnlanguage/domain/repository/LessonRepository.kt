package com.nhom2.elearnlanguage.domain.repository

import com.nhom2.elearnlanguage.domain.model.lesson.Question

interface LessonRepository {
    
    suspend fun getLessonQuestions(lessonId: Int): List<Question>
}
