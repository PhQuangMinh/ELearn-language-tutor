package com.nhom2.elearnlanguage.data.dto

data class HomeDataDTO(
    val fullName: String = "",
    val currentLessons: List<CurrentLessonDTO> = emptyList(),
    val courses: List<CourseProgressDTO> = emptyList()
)
