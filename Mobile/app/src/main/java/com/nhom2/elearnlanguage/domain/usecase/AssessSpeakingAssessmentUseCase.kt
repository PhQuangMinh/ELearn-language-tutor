package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.model.lesson.SpeakingAssessmentResult
import com.nhom2.elearnlanguage.domain.repository.LessonRepository
import java.io.File
import javax.inject.Inject

class AssessSpeakingAssessmentUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    suspend operator fun invoke(
        referenceText: String,
        audioFile: File,
        language: String = "US_ENGLISH"
    ): Result<SpeakingAssessmentResult> {
        return try {
            val result = lessonRepository.assessSpeaking(referenceText, audioFile, language)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
