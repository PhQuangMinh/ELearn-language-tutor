package com.nhom2.elearnlanguage.domain.repository

import com.nhom2.elearnlanguage.domain.model.Flashcard

interface VocabularyRepository {
    /**
     * Get flashcards - optionally filtered by topic ID
     * @param topicId optional topic ID to filter flashcards. If null, returns all flashcards
     */
    suspend fun getFlashcards(topicId: Int? = null): List<Flashcard>

    /**
     * Get a single flashcard by ID
     */
    suspend fun getFlashcardById(id: Int): Flashcard
}