package com.nhom2.elearnlanguage.domain.repository

interface DeviceTokenRepository {
    suspend fun registerCurrentDeviceToken(token: String)
    suspend fun unregisterCurrentDeviceToken(token: String)
}
