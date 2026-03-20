package com.nhom2.elearnlanguage.domain.repository

import com.nhom2.elearnlanguage.domain.model.ImproveTextResult

interface ImproveRepository {
    suspend fun improveMessage(message: String): ImproveTextResult
}
