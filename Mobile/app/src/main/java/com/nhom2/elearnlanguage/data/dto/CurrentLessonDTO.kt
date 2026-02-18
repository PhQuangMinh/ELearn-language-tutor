package com.nhom2.elearnlanguage.data.dto

data class CurrentLessonDTO(
    val id: Int = 0,
    val lessonNumber: Int = 0,
    val title: String = "",
    val level: String = "",
    val progressPercent: Int = 0
)
