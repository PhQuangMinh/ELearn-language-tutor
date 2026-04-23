package com.nhom2.elearnlanguage.domain.model.speaking

data class LessonScenario(
    val id: Int,
    val title: String,
    val description: String,
    val tasks: String,
    val openingMessage: String,
    val suggestion: String,
    val translation: String
)

