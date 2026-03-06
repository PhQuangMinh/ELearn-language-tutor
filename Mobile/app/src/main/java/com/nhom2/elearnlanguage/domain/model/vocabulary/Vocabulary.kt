package com.nhom2.elearnlanguage.domain.model.vocabulary

data class Vocabulary(
    val word: String,
    val meaning: String,
    val pronunciation: String?,
    val type: VocabularyType,
    val definition: String? = null,
    val example: String? = null
)

