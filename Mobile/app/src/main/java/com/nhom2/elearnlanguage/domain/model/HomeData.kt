package com.nhom2.elearnlanguage.domain.model

data class HomeData(
    val fullName: String,
    val currentLessons: List<CurrentLesson>,
    val courses: List<CourseProgress>
)
