package com.nhom2.elearnlanguage.domain.model

data class CurrentLesson(
    val id: Int,
    val lessonNumber: Int,
    val title: String,
    val level: String,
    val progressPercent: Int
)
