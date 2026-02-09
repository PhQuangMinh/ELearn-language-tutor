package com.nhom2.elearnlanguage.data.mapper

import com.nhom2.elearnlanguage.data.dto.AuthResponseDTO
import com.nhom2.elearnlanguage.domain.model.AuthSession
import com.nhom2.elearnlanguage.domain.model.User

object AuthMapper {
    fun toAuthSession(dto: AuthResponseDTO): AuthSession {
        return AuthSession(
            accessToken = dto.token,
            refreshToken = dto.refreshToken,
            user = User (
                id = dto.id,
                username = dto.username,
                email = dto.email,
                fullName = dto.fullName,
                role = dto.role
            )
        )
    }
}