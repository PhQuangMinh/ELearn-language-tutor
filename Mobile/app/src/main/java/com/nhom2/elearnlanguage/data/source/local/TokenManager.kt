package com.nhom2.elearnlanguage.data.source.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object TokenManager {
    private const val PREF_NAME = "auth_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveAccessToken(context: Context, accessToken: String) {
        getPrefs(context).edit { putString(KEY_ACCESS_TOKEN, accessToken) }
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

    fun clearTokens(context: Context) {
        getPrefs(context)
            .edit {
                remove(KEY_ACCESS_TOKEN)
                    .remove(KEY_REFRESH_TOKEN)
            }
    }
}