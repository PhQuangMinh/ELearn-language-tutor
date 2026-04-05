package com.nhom2.elearnlanguage.domain.model.lesson

data class SpeakingAssessmentResult(
    val recognitionStatus: String,
    val displayText: String,
    val audioUrl: String,
    val overall: SpeakingAssessmentOverall,
    val words: List<SpeakingAssessmentWord> = emptyList()
)

data class SpeakingAssessmentOverall(
    val accuracyScore: Double,
    val fluencyScore: Double,
    val prosodyScore: Double,
    val completenessScore: Double,
    val pronScore: Double
)

data class SpeakingAssessmentWord(
    val word: String,
    val accuracyScore: Double,
    val errorType: String
)
