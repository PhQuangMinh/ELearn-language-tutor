package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.model.speaking.AiRespondRequest
import com.nhom2.elearnlanguage.domain.model.speaking.AiRespondResult
import com.nhom2.elearnlanguage.domain.repository.SpeakingRepository
import javax.inject.Inject

class AiRespondUseCase @Inject constructor(
    private val speakingRepository: SpeakingRepository
) {
    suspend operator fun invoke(request: AiRespondRequest): AiRespondResult {
        return speakingRepository.aiRespond(request)
    }
}

