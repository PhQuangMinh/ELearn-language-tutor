package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.model.vocabulary.Vocabulary
import com.nhom2.elearnlanguage.domain.repository.VocabularyRepository
import javax.inject.Inject

class GetTopicVocabulariesUseCase @Inject constructor(
    private val vocabularyRepository: VocabularyRepository
) {
    suspend operator fun invoke(topicId: Int): List<Vocabulary> {
        return vocabularyRepository.getTopicVocabularies(topicId)
    }
}

