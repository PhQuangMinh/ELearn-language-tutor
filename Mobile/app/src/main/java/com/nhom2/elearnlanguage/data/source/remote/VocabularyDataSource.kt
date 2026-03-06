package com.nhom2.elearnlanguage.data.source.remote

import com.nhom2.elearnlanguage.BuildConfig
import com.nhom2.elearnlanguage.data.dto.ApiResponseDTO
import com.nhom2.elearnlanguage.data.dto.FlashCardDTO
import com.nhom2.elearnlanguage.domain.model.Flashcard
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

class VocabularyDataSource @Inject constructor(
    private val httpClient: HttpClient
){
    /**
     * Get flashcards - optionally filtered by topic ID
     * @param topicId optional topic ID to filter flashcards. If null, returns all flashcards
     */
    suspend fun getFlashcards(topicId: Int? = null): ApiResponseDTO<List<FlashCardDTO>> {
        val url = if (topicId != null) {
            "${BuildConfig.API_BASE_URL}/api/flashcards?topicId=$topicId"
        } else {
            "${BuildConfig.API_BASE_URL}/api/flashcards"
        }
        return httpClient.get(url).body()
    }

    /**
     * Get a single flashcard by ID
     */
    suspend fun getFlashcardById(id: Int): ApiResponseDTO<FlashCardDTO> {
        return httpClient.get("${BuildConfig.API_BASE_URL}/api/flashcards/$id").body()
    }
}