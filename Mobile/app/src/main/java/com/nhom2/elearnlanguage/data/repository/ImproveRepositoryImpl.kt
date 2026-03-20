package com.nhom2.elearnlanguage.data.repository

import com.nhom2.elearnlanguage.data.source.remote.ImproveDataSource
import com.nhom2.elearnlanguage.domain.model.ImproveTextResult
import com.nhom2.elearnlanguage.domain.repository.ImproveRepository
import javax.inject.Inject

class ImproveRepositoryImpl @Inject constructor(
    private val improveDataSource: ImproveDataSource
) : ImproveRepository {

    override suspend fun improveMessage(message: String): ImproveTextResult {
        val response = improveDataSource.improveMessage(message)
        val payload = response.data

        if (!response.success || payload == null) {
            throw Exception(response.message)
        }

        val improved = payload.improved?.trim().orEmpty()
        val explanation = payload.explanation?.trim().orEmpty()

        if (improved.isBlank() || explanation.isBlank()) {
            throw Exception("Improve API response is missing required fields")
        }

        return ImproveTextResult(
            original = payload.original?.trim().orEmpty().ifBlank { message },
            improved = improved,
            explanation = explanation
        )
    }
}
