package com.nhom2.elearnlanguage.data.dto

data class DeviceTokenRequestDTO(
    val token: String,
    val deviceId: String?,
    val appVersion: String,
    val platform: String = "ANDROID"
)
