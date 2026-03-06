package com.nhom2.elearnlanguage.data.dto.vocabulary

import com.google.gson.annotations.SerializedName

data class TopicVocabularyResponseDTO(
    @SerializedName("topicId")
    val topicId: Int,
    @SerializedName("vocabulary")
    val vocabulary: List<VocabularyDTO>
)

