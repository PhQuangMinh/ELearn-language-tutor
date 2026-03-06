package com.nhom2.elearnlanguage.data.mapper

import com.nhom2.elearnlanguage.data.dto.FlashCardDTO
import com.nhom2.elearnlanguage.domain.model.Flashcard

fun FlashCardDTO.toDomain(): Flashcard {
    return Flashcard(
        id = this.id,
        word = this.word,
        pronunciation = this.pronunciation,
        meaning = this.meaning,
        imageUrl = this.imageUrl,
        example = this.example

    )
}