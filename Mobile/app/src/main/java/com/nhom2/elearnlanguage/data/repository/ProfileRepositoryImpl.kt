package com.nhom2.elearnlanguage.data.repository

import com.nhom2.elearnlanguage.data.mapper.toDomain
import com.nhom2.elearnlanguage.data.source.remote.ProfileDataSource
import com.nhom2.elearnlanguage.domain.model.UserProfile
import com.nhom2.elearnlanguage.domain.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileDataSource: ProfileDataSource
) : ProfileRepository {
    override suspend fun getMyProfile(): UserProfile {
        val response = profileDataSource.getMyProfile()
        if (!response.success || response.data == null) {
            throw Exception(response.message)
        }
        return response.data.toDomain()
    }

    override suspend fun updateMyProfile(
        fullName: String,
        avatarBytes: ByteArray?,
        avatarFileName: String?,
        avatarMimeType: String?
    ): UserProfile {
        val response = profileDataSource.updateMyProfile(
            fullName = fullName,
            avatarBytes = avatarBytes,
            avatarFileName = avatarFileName,
            avatarMimeType = avatarMimeType
        )
        if (!response.success || response.data == null) {
            throw Exception(response.message)
        }
        return response.data.toDomain()
    }
}
