package com.nhom2.elearnlanguage.data.dto.vocabulary

import com.google.gson.annotations.SerializedName

data class VocabularyDTO(
    @SerializedName("word")
    val word: String,
    @SerializedName("meaning")
    val meaning: String,
    @SerializedName("pronunciation")
    val pronunciation: String? = null,
    /**
     * Backend doc uses: "NOUN", "VERB", "ADJECTIVE"
     */
    @SerializedName("type")
    val type: String,
    // Not in current API doc, but useful for the UI in mock mode.
    @SerializedName("definition")
    val definition: String? = null,
    @SerializedName("example")
    val example: String? = null
)

