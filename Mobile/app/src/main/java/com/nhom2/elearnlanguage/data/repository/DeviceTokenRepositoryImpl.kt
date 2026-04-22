package com.nhom2.elearnlanguage.data.repository

import android.content.Context
import android.provider.Settings
import com.nhom2.elearnlanguage.BuildConfig
import com.nhom2.elearnlanguage.data.dto.DeviceTokenRequestDTO
import com.nhom2.elearnlanguage.data.source.remote.DeviceTokenDataSource
import com.nhom2.elearnlanguage.domain.repository.DeviceTokenRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeviceTokenRepositoryImpl @Inject constructor(
    private val dataSource: DeviceTokenDataSource,
    @ApplicationContext private val context: Context
) : DeviceTokenRepository {

    override suspend fun registerCurrentDeviceToken(token: String) {
        val request = DeviceTokenRequestDTO(
            token = token,
            deviceId = getAndroidDeviceId(),
            appVersion = BuildConfig.VERSION_NAME,
            platform = "ANDROID"
        )
        val response = dataSource.registerDeviceToken(request)
        if (!response.success) {
            throw Exception(response.message)
        }
    }

    override suspend fun unregisterCurrentDeviceToken(token: String) {
        val response = dataSource.unregisterDeviceToken(token)
        if (!response.success) {
            throw Exception(response.message)
        }
    }

    private fun getAndroidDeviceId(): String? {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
