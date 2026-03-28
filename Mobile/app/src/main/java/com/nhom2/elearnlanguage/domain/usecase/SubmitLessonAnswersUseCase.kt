package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.data.dto.lesson.LessonSubmitRequest
import com.nhom2.elearnlanguage.domain.model.lesson.LessonSubmitResult
import com.nhom2.elearnlanguage.domain.repository.LessonRepository
import com.nhom2.elearnlanguage.domain.repository.StreakRepository
import com.nhom2.elearnlanguage.domain.repository.TokenStorage
import javax.inject.Inject

class SubmitLessonAnswersUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val streakRepository: StreakRepository,
    private val tokenStorage: TokenStorage
) {
    suspend operator fun invoke(lessonId: Int, request: LessonSubmitRequest): Result<LessonSubmitResult> {
        return try {
            val submitResult = lessonRepository.submitLessonAnswers(lessonId, request)
            if (submitResult.streakExtended) {
                tokenStorage.getUserId()?.let { userId ->
                    streakRepository.updateCacheIfPresent(
                        userId = userId,
                        currentStreak = submitResult.currentStreak
                    )
                }
            }
            Result.success(submitResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

