package com.nhom2.elearnlanguage.domain.usecase

import com.google.firebase.messaging.FirebaseMessaging
import com.nhom2.elearnlanguage.domain.repository.DeviceTokenRepository
import com.nhom2.elearnlanguage.domain.repository.TokenStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SyncFcmTokenUseCase @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val deviceTokenRepository: DeviceTokenRepository
) {

    suspend operator fun invoke(explicitToken: String? = null) {
        val token = resolveToken(explicitToken) ?: return
        tokenStorage.saveFcmToken(token)

        if (tokenStorage.getUserId() == null) {
            return
        }

        deviceTokenRepository.registerCurrentDeviceToken(token)
    }

    private suspend fun resolveToken(explicitToken: String?): String? {
        if (!explicitToken.isNullOrBlank()) {
            return explicitToken
        }

        val cachedToken = tokenStorage.getFcmToken()
        if (!cachedToken.isNullOrBlank()) {
            return cachedToken
        }

        return runCatching {
            FirebaseMessaging.getInstance().token.await()
        }.getOrNull()
    }
}
