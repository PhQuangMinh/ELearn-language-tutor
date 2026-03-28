package com.nhom2.elearnlanguage.data.source.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object TokenManager {
    private const val PREF_NAME = "auth_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "user_id"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveAccessToken(context: Context, accessToken: String) {
        getPrefs(context).edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
            val userIdFromToken = extractUserIdFromJwt(accessToken)
            if (userIdFromToken != null) {
                putLong(KEY_USER_ID, userIdFromToken)
            } else {
                remove(KEY_USER_ID)
            }
        }
    }

    fun saveRefreshToken(context: Context, token: String?) {
        token ?: return
        getPrefs(context)
            .edit {
                putString(KEY_REFRESH_TOKEN, token)
            }
    }

    fun getAccessToken(context: Context): String? {
        return getPrefs(context).getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(context: Context): String? {
        return getPrefs(context).getString(KEY_REFRESH_TOKEN, null)
    }

    fun saveUserId(context: Context, userId: Long?) {
        getPrefs(context).edit {
            if (userId == null) {
                remove(KEY_USER_ID)
            } else {
                putLong(KEY_USER_ID, userId)
            }
        }
    }

    fun getUserId(context: Context): Long? {
        val prefs = getPrefs(context)
        if (!prefs.contains(KEY_USER_ID)) {
            return null
        }
        return prefs.getLong(KEY_USER_ID, -1L).takeIf { it > 0L }
    }

    fun clearTokens(context: Context) {
        getPrefs(context)
            .edit {
                remove(KEY_ACCESS_TOKEN)
                    .remove(KEY_REFRESH_TOKEN)
                    .remove(KEY_USER_ID)
            }
    }

    private fun extractUserIdFromJwt(accessToken: String): Long? {
        return try {
            val parts = accessToken.split(".")
            if (parts.size < 2) return null
            val payload = parts[1]
            val decoded = android.util.Base64.decode(
                payload,
                android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING
            )
            val payloadJson = org.json.JSONObject(String(decoded))
            when {
                payloadJson.has("userId") -> payloadJson.optLong("userId").takeIf { it > 0L }
                payloadJson.has("id") -> payloadJson.optLong("id").takeIf { it > 0L }
                payloadJson.has("user_id") -> payloadJson.optLong("user_id").takeIf { it > 0L }
                payloadJson.has("sub") -> payloadJson.optString("sub").toLongOrNull()
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}