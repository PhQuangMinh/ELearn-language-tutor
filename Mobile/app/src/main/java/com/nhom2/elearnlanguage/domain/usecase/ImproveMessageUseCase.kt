package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.model.speaking.ImproveTextResult
import com.nhom2.elearnlanguage.domain.repository.ImproveRepository
import javax.inject.Inject

class ImproveMessageUseCase @Inject constructor(
    private val improveRepository: ImproveRepository
) {
    suspend operator fun invoke(message: String): ImproveTextResult {
        return improveRepository.improveMessage(message)
    }
}
