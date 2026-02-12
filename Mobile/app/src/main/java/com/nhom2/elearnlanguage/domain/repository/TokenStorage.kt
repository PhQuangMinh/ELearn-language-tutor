package com.nhom2.elearnlanguage.domain.repository

interface TokenStorage {
    fun saveAccessToken(token: String)
    fun saveRefreshToken(token: String?)
    fun clearTokens()
}