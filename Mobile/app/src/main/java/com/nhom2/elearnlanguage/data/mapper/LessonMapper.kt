package com.nhom2.elearnlanguage.data.mapper

import com.nhom2.elearnlanguage.data.dto.LessonInTopicDTO
import com.nhom2.elearnlanguage.domain.model.LessonInTopic

object LessonMapper {
    fun toLessonInTopic(dto: LessonInTopicDTO): LessonInTopic {
        return LessonInTopic(
            id = dto.id,
            name = dto.name,
            imageUrl = dto.imageUrl,
            completed = dto.completed
        )
    }
}

