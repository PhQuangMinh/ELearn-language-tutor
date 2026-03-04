package com.nhom2.elearnlanguage.data.dto

data class CourseProgressDTO(
    val id: Int = 0,
    val title: String = "",
    val imageUrl: String? = null,
    val progressPercent: Int = 0
)
