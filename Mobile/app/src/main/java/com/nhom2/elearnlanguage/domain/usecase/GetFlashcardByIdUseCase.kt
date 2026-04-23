package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.model.Flashcard
import com.nhom2.elearnlanguage.domain.repository.VocabularyRepository
import javax.inject.Inject

class GetFlashcardByIdUseCase @Inject constructor(
    private val vocabularyRepository: VocabularyRepository
) {
    suspend operator fun invoke(id: Int): Flashcard {
        return vocabularyRepository.getFlashcardById(id)
    }
}
