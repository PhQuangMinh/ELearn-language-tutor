package com.nhom2.elearnlanguage.domain.repository

import kotlinx.coroutines.flow.Flow

interface SpeechToTextRepository {
    fun startListening()
    fun stopListening()
    fun getResults(): Flow<String>
}