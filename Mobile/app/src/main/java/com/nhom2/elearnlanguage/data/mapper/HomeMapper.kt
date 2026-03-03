package com.nhom2.elearnlanguage.data.mapper

import com.nhom2.elearnlanguage.data.dto.CourseProgressDTO
import com.nhom2.elearnlanguage.data.dto.HomeDataDTO
import com.nhom2.elearnlanguage.domain.model.CourseProgress
import com.nhom2.elearnlanguage.domain.model.HomeData

fun CourseProgressDTO.toDomain(): CourseProgress = CourseProgress(
    id = id,
    title = title,
    imageUrl = imageUrl,
    progressPercent = progressPercent
)

fun HomeDataDTO.toDomain(): HomeData = HomeData(
    fullName = fullName,
    courses = courses.map { it.toDomain() }
)
