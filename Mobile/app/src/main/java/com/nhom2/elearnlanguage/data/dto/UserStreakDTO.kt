package com.nhom2.elearnlanguage.data.dto

import java.time.LocalDateTime

data class UserStreakDTO(
    val id: Long = 0L,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastStreakUpdated: LocalDateTime? = null
)
