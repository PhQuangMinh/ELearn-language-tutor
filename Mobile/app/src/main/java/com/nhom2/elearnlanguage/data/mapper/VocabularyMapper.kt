package com.nhom2.elearnlanguage.data.mapper

import com.nhom2.elearnlanguage.data.dto.vocabulary.VocabularyDTO
import com.nhom2.elearnlanguage.domain.model.vocabulary.Vocabulary
import com.nhom2.elearnlanguage.domain.model.vocabulary.VocabularyType

fun VocabularyDTO.toDomain(): Vocabulary {
    val typeEnum = runCatching { VocabularyType.valueOf(type) }.getOrElse { VocabularyType.NOUN }
    return Vocabulary(
        word = word,
        meaning = meaning,
        pronunciation = pronunciation,
        type = typeEnum,
        definition = definition,
        example = example
    )
}

