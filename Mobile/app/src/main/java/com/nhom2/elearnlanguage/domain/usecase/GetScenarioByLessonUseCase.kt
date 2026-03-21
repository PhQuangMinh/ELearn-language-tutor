package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.model.speaking.LessonScenario
import com.nhom2.elearnlanguage.domain.repository.SpeakingRepository
import javax.inject.Inject

class GetScenarioByLessonUseCase @Inject constructor(
    private val speakingRepository: SpeakingRepository
) {
    suspend operator fun invoke(lessonId: Int): LessonScenario {
        return speakingRepository.getScenarioByLesson(lessonId)
    }
}

