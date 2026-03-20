package com.nhom2.elearnlanguage.data.dto

data class ScenarioDetailResponseDTO(
    val id: Int,
    val title: String,
    val description: String,
    val tasks: String,
    val openningMessage: String,
    val suggestion: String,
    val translation: String
)

