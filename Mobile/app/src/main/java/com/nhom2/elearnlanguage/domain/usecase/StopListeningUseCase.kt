package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.repository.SpeechToTextRepository
import javax.inject.Inject

class StopListeningUseCase @Inject constructor(
    private val speechToTextRepository: SpeechToTextRepository
) {
    operator fun invoke() = speechToTextRepository.stopListening()
}