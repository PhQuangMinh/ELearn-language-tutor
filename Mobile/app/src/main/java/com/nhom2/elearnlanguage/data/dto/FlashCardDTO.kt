package com.nhom2.elearnlanguage.data.dto

data class FlashCardDTO(
    val id: Long,
    val word: String? = null,
    val pronunciation: String? = null,
    val meaning: String? = null,
    val example: String? = null,
    val imageUrl: String? = null
)
