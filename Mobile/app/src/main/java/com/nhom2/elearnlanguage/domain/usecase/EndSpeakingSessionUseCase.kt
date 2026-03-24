package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.repository.SpeakingRepository
import javax.inject.Inject

class EndSpeakingSessionUseCase @Inject constructor(
    private val speakingRepository: SpeakingRepository
) {
    suspend operator fun invoke(sessionId: Int) {
        speakingRepository.endSpeakingSession(sessionId)
    }
}