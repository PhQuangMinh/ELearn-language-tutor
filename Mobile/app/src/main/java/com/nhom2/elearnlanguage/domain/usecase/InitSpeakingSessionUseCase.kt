package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.repository.SpeakingRepository
import javax.inject.Inject

class InitSpeakingSessionUseCase @Inject constructor(
    private val speakingRepository: SpeakingRepository
) {
    suspend operator fun invoke(scenarioId: Int): Int {
        return speakingRepository.initSpeakingSession(scenarioId)
    }
}