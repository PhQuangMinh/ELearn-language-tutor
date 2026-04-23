package com.nhom2.elearnlanguage.domain.repository

import com.nhom2.elearnlanguage.domain.model.UserProfile

interface ProfileRepository {
    suspend fun getMyProfile(): UserProfile
    suspend fun updateMyProfile(
        fullName: String,
        avatarBytes: ByteArray?,
        avatarFileName: String?,
        avatarMimeType: String?
    ): UserProfile
}
