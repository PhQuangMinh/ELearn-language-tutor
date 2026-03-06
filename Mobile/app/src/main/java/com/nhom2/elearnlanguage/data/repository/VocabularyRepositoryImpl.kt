package com.nhom2.elearnlanguage.data.repository

import com.nhom2.elearnlanguage.data.mapper.toDomain
import com.nhom2.elearnlanguage.data.source.remote.VocabularyDataSource
import com.nhom2.elearnlanguage.domain.model.Flashcard
import com.nhom2.elearnlanguage.domain.repository.VocabularyRepository
import java.lang.Exception
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
}