package com.nhom2.elearnlanguage.domain.repository

import com.nhom2.elearnlanguage.domain.model.UserStreak

interface StreakRepository {
    suspend fun getUserStreak(userId: Long, forceRefresh: Boolean = false): UserStreak
    fun updateCacheIfPresent(userId: Long, currentStreak: Int)
    fun clearCache()
}
