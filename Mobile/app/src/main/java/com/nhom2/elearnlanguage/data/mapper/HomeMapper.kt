package com.nhom2.elearnlanguage.data.mapper

import com.nhom2.elearnlanguage.data.dto.CourseProgressDTO
import com.nhom2.elearnlanguage.data.dto.CurrentLessonDTO
import com.nhom2.elearnlanguage.data.dto.HomeDataDTO
import com.nhom2.elearnlanguage.domain.model.CourseProgress
import com.nhom2.elearnlanguage.domain.model.CurrentLesson
import com.nhom2.elearnlanguage.domain.model.HomeData

fun CurrentLessonDTO.toDomain(): CurrentLesson = CurrentLesson(
    id = id,
    lessonNumber = lessonNumber,
    title = title,
    level = level,
    progressPercent = progressPercent
)

fun CourseProgressDTO.toDomain(): CourseProgress = CourseProgress(
    id = id,
    title = title,
    imageUrl = imageUrl,
    progressPercent = progressPercent
)

fun HomeDataDTO.toDomain(): HomeData = HomeData(
    fullName = fullName,
    currentLessons = currentLessons.map { it.toDomain() },
    courses = courses.map { it.toDomain() }
)
