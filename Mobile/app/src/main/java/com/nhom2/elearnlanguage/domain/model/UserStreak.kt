package com.nhom2.elearnlanguage.domain.model

import java.time.LocalDateTime

data class UserStreak(
    val id: Long,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastStreakUpdated: LocalDateTime?
)
