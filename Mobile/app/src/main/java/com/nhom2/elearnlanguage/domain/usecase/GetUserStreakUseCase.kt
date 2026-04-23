package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.model.UserStreak
import com.nhom2.elearnlanguage.domain.repository.StreakRepository
import javax.inject.Inject

class GetUserStreakUseCase @Inject constructor(
    private val streakRepository: StreakRepository
) {
    suspend operator fun invoke(userId: Long, forceRefresh: Boolean = false): UserStreak {
        return streakRepository.getUserStreak(userId, forceRefresh)
    }
}
