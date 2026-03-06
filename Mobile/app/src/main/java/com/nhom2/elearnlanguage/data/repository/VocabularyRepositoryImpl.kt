package com.nhom2.elearnlanguage.data.repository

import com.nhom2.elearnlanguage.data.mapper.toDomain
import com.nhom2.elearnlanguage.data.source.remote.VocabularyDataSource
import com.nhom2.elearnlanguage.domain.model.Flashcard
import java.lang.Exception
import com.nhom2.elearnlanguage.domain.model.vocabulary.Vocabulary
import com.nhom2.elearnlanguage.domain.repository.VocabularyRepository
import javax.inject.Inject

class VocabularyRepositoryImpl @Inject constructor(
    private val vocabularyDataSource: VocabularyDataSource
): VocabularyRepository {
    override suspend fun getFlashcards(topicId: Int?): List<Flashcard> {
        val response = vocabularyDataSource.getFlashcards(topicId)

        if(!response.success || response.data == null) {
            throw Exception(response.message)
        }

        return response.data.map { it.toDomain() }
    }

    override suspend fun getFlashcardById(id: Int): Flashcard {
        val response = vocabularyDataSource.getFlashcardById(id)

        if(!response.success || response.data == null) {
            throw Exception(response.message)
        }

        return response.data.toDomain()
    }

    override suspend fun getTopicVocabularies(topicId: Int): List<Vocabulary> {
        val response = vocabularyDataSource.getTopicVocabularies(topicId)
        return response.vocabulary.map { it.toDomain() }
    }
}

