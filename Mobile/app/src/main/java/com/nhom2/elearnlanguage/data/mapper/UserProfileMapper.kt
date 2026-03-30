package com.nhom2.elearnlanguage.data.mapper

import com.nhom2.elearnlanguage.data.dto.UserProfileDTO
import com.nhom2.elearnlanguage.domain.model.UserProfile

fun UserProfileDTO.toDomain(): UserProfile = UserProfile(
    fullName = fullName?.trim().orEmpty(),
    email = email.orEmpty(),
    avatarUrl = avatarUrl,
    provider = provider
)
