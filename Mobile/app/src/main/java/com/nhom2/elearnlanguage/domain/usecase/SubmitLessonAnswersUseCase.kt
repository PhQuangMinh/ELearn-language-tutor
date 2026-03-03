package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.data.dto.lesson.LessonSubmitRequest
import com.nhom2.elearnlanguage.domain.repository.LessonRepository
import javax.inject.Inject

class SubmitLessonAnswersUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    suspend operator fun invoke(lessonId: Int, request: LessonSubmitRequest): Result<Boolean> {
        return try {
            val ok = lessonRepository.submitLessonAnswers(lessonId, request)
            Result.success(ok)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

