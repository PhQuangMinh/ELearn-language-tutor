package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.model.Flashcard
import com.nhom2.elearnlanguage.domain.repository.VocabularyRepository
import javax.inject.Inject

class GetFlashcardListOfTopicUseCase @Inject constructor(
    private val vocabularyRepository: VocabularyRepository
) {
    suspend operator fun invoke(topicId: Int? = null): List<Flashcard> {
        return vocabularyRepository.getFlashcards(topicId)
    }
}