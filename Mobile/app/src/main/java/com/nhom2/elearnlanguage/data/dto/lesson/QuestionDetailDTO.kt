package com.nhom2.elearnlanguage.data.dto.lesson

import kotlinx.serialization.Serializable

@Serializable
data class QuestionDetailDTO(
    val id: Int,
    val type: String, // "ONE_SELECTION", "LISTEN_AND_ARRANGE_SENTENCE", "TRANSLATE_AND_ARRANGE_SENTENCE"
    val content: String,
    val media: MediaDTO? = null,
    val answers: List<AnswerDTO> = emptyList()
)
