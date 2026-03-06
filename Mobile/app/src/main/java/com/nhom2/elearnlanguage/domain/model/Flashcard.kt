package com.nhom2.elearnlanguage.domain.model

data class Flashcard (
    val id: Long,
    val word: String?,
    val pronunciation: String?,
    val meaning: String?,
    val example: String?,
    val imageUrl: String?
)