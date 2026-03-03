package com.nhom2.elearnlanguage.data.dto.lesson

data class LessonSubmitRequest(
    val startedAt: String,
    val endedAt: String,
    val questionAnswers: List<QuestionAnswerItem>
)

data class QuestionAnswerItem(
    val id: Int,
    val answer: SubmittedAnswer
)

data class SubmittedAnswer(
    val id: Int,
    val content: String
)

