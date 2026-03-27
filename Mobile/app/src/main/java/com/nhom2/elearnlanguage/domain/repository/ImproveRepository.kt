package com.nhom2.elearnlanguage.domain.repository

import com.nhom2.elearnlanguage.domain.model.speaking.ImproveTextResult

interface ImproveRepository {
    suspend fun improveMessage(text: String, context: String): ImproveTextResult
}
