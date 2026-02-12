package com.nhom2.elearnlanguage.data.source.local

import android.content.Context
import com.nhom2.elearnlanguage.domain.repository.TokenStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStorageImpl @Inject constructor(
    private val context: Context
) : TokenStorage {

    override fun saveAccessToken(token: String) {
        TokenManager.saveAccessToken(context, token)
    }

    override fun saveRefreshToken(token: String?) {
        TokenManager.saveRefreshToken(context, token)
    }

    override fun clearTokens() {
        TokenManager.clearTokens(context)
    }
}