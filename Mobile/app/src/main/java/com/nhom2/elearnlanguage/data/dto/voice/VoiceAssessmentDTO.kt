package com.nhom2.elearnlanguage.data.dto.voice

data class VoiceAssessmentDTO(
    val recognitionStatus: String,
    val displayText: String,
    val audioUrl: String,
    val overall: VoiceAssessmentOverallDTO,
    val words: List<VoiceAssessmentWordDTO> = emptyList()
)

data class VoiceAssessmentOverallDTO(
    val accuracyScore: Double,
    val fluencyScore: Double,
    val prosodyScore: Double,
    val completenessScore: Double,
    val pronScore: Double
)

data class VoiceAssessmentWordDTO(
    val word: String,
    val accuracyScore: Double,
    val errorType: String
)
