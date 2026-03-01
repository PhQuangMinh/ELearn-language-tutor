package com.nhom2.elearnlanguage.domain.repository

import com.nhom2.elearnlanguage.domain.model.lesson.Question
import com.nhom2.elearnlanguage.data.dto.lesson.LessonSubmitRequest

interface LessonRepository {
    
    suspend fun getLessonQuestions(lessonId: Int): List<Question>

    suspend fun submitLessonAnswers(lessonId: Int, request: LessonSubmitRequest): Boolean
}
