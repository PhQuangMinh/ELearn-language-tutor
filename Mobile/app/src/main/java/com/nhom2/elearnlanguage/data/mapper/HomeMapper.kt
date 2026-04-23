package com.nhom2.elearnlanguage.data.mapper

import com.nhom2.elearnlanguage.data.dto.CourseProgressDTO
import com.nhom2.elearnlanguage.data.dto.HomeDataDTO
import com.nhom2.elearnlanguage.data.dto.UserStreakDTO
import com.nhom2.elearnlanguage.domain.model.CourseProgress
import com.nhom2.elearnlanguage.domain.model.HomeData
import com.nhom2.elearnlanguage.domain.model.UserStreak

fun CourseProgressDTO.toDomain(): CourseProgress = CourseProgress(
    id = id,
    title = title,
    imageUrl = imageUrl ?: "",
    progressPercent = progressPercent
)

fun HomeDataDTO.toDomain(): HomeData = HomeData(
    fullName = fullName,
    courses = courses.map { it.toDomain() }
)

fun UserStreakDTO.toDomain(): UserStreak = UserStreak(
    id = id,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastStreakUpdated = lastStreakUpdated
)
