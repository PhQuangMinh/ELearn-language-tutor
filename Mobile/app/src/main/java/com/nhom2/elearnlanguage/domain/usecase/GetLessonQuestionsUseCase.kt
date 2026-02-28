package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.model.lesson.Question
import com.nhom2.elearnlanguage.domain.repository.LessonRepository
import javax.inject.Inject

class GetLessonQuestionsUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    
    suspend operator fun invoke(lessonId: Int): Result<List<Question>> {
        return try {
            val questions = lessonRepository.getLessonQuestions(lessonId)
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
