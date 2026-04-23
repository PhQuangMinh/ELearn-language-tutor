package com.nhom2.elearnlanguage.domain.model.lesson

enum class QuestionType {
    ONE_SELECTION,
    LISTEN_AND_ARRANGE_SENTENCE,
    TRANSLATE_AND_ARRANGE_SENTENCE,
    SPEAKING_ASSESSMENT
}

data class Question(
    val id: Int,
    val type: QuestionType,
    val content: String,
    val repeatable: Boolean = false,
    val media: Media? = null,
    val answers: List<Answer> = emptyList()
) {
    /**
     * Get the correct answer for arrange sentence questions
     */
    fun getCorrectAnswer(): Answer? = answers.firstOrNull { it.correct }
    
    /**
     * Get words from correct answer for word bank
     */
    fun getWordsForBank(): List<String> {
        return getCorrectAnswer()?.content
            ?.split(" ")
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }
    
    /**
     * Check if user's arranged sentence is correct
     */
    fun checkArrangedSentence(userWords: List<String>): Boolean {
        val correctSentence = getCorrectAnswer()?.content?.trim()
            ?.replace(Regex("\\s+"), " ") ?: return false
        val userSentence = userWords.joinToString(" ").trim()
            .replace(Regex("\\s+"), " ")
        return correctSentence.equals(userSentence, ignoreCase = true)
    }
}
