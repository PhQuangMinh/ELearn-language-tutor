package com.nhom2.elearnlanguage.domain.repository

interface TokenStorage {
    fun saveAccessToken(token: String)
    fun saveRefreshToken(token: String?)
    fun getRefreshToken(): String?
    fun saveUserId(userId: Long?)
    fun getUserId(): Long?
    fun saveFcmToken(token: String?)
    fun getFcmToken(): String?
    fun clearTokens()
}