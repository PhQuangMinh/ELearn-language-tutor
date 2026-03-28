package com.nhom2.elearnlanguage.data.repository

import android.util.Log
import com.nhom2.elearnlanguage.data.mapper.toDomain
import com.nhom2.elearnlanguage.data.source.remote.StreakDataSource
import com.nhom2.elearnlanguage.domain.model.UserStreak
import com.nhom2.elearnlanguage.domain.repository.StreakRepository
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakRepositoryImpl @Inject constructor(
    private val streakDataSource: StreakDataSource
) : StreakRepository {

    private data class CachedStreak(
        val userId: Long,
        val data: UserStreak,
        val cachedAtMs: Long
    )

    private var streakCache: CachedStreak? = null
    private val streakTtlMs = 30 * 60 * 1000L // 30 minutes

    override suspend fun getUserStreak(userId: Long, forceRefresh: Boolean): UserStreak {
        val now = System.currentTimeMillis()
        val cache = streakCache
        if (!forceRefresh &&
            cache != null &&
            cache.userId == userId &&
            now - cache.cachedAtMs <= streakTtlMs
        ) {
            Log.d("STREAK_CACHE", "hit userId=$userId")
            return cache.data
        }

        val response = streakDataSource.getUserStreak(userId)
        if (!response.success || response.data == null) {
            Log.e("STREAK_API", "getUserStreak failed: ${response.message}")
            throw Exception(response.message)
        }

        val freshData = response.data!!.toDomain()
        streakCache = CachedStreak(
            userId = userId,
            data = freshData,
            cachedAtMs = now
        )
        Log.d("STREAK_CACHE", "store userId=$userId")
        return freshData
    }

    override fun clearCache() {
        streakCache = null
    }

    override fun updateCacheIfPresent(userId: Long, currentStreak: Int) {
        val cache = streakCache ?: return
        if (cache.userId != userId) return

        val updatedStreak = cache.data.copy(
            currentStreak = currentStreak,
            longestStreak = maxOf(cache.data.longestStreak, currentStreak),
            lastStreakUpdated = LocalDateTime.now()
        )
        streakCache = cache.copy(
            data = updatedStreak,
            cachedAtMs = System.currentTimeMillis()
        )
        Log.d("STREAK_CACHE", "updated from lesson submit userId=$userId")
    }
}
