package com.nhom2.elearnlanguage.data.dto

data class HomeDataDTO(
    val fullName: String = "",
    val courses: List<CourseProgressDTO> = emptyList()
)
