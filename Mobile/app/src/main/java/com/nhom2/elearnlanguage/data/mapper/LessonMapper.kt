package com.nhom2.elearnlanguage.data.mapper

import com.nhom2.elearnlanguage.data.dto.lesson.AnswerDTO
import com.nhom2.elearnlanguage.data.dto.lesson.LessonSubmitResultDTO
import com.nhom2.elearnlanguage.data.dto.lesson.MediaDTO
import com.nhom2.elearnlanguage.data.dto.lesson.QuestionDetailDTO
import com.nhom2.elearnlanguage.data.dto.voice.VoiceAssessmentDTO
import com.nhom2.elearnlanguage.domain.model.lesson.Answer
import com.nhom2.elearnlanguage.domain.model.lesson.LessonSubmitResult
import com.nhom2.elearnlanguage.domain.model.lesson.Media
import com.nhom2.elearnlanguage.domain.model.lesson.MediaType
import com.nhom2.elearnlanguage.domain.model.lesson.Question
import com.nhom2.elearnlanguage.domain.model.lesson.QuestionType
import com.nhom2.elearnlanguage.domain.model.lesson.SpeakingAssessmentOverall
import com.nhom2.elearnlanguage.domain.model.lesson.SpeakingAssessmentResult
import com.nhom2.elearnlanguage.domain.model.lesson.SpeakingAssessmentWord
import com.nhom2.elearnlanguage.data.dto.LessonInTopicDTO
import com.nhom2.elearnlanguage.domain.model.LessonInTopic

object LessonMapper {
    fun toLessonInTopic(dto: LessonInTopicDTO): LessonInTopic {
        return LessonInTopic(
            id = dto.id,
            name = dto.name,
            imageUrl = dto.imageUrl,
            completed = dto.completed
        )
    }
}

fun AnswerDTO.toDomain(): Answer {
    return Answer(
        id = id,
        content = content,
        correct = correct
    )
}

fun MediaDTO.toDomain (mediaDto: MediaDTO): Media {
    return Media (
        id = id,
        name = name,
        size = size,
        when (type) {
            "IMAGE" -> MediaType.IMAGE
            "AUDIO" -> MediaType.AUDIO
            else -> MediaType.IMAGE
        },
        url = url
    )
}

fun QuestionDetailDTO.toDomain(): Question {
    return Question (
        id = id,
        type = when (type) {
            "ONE_SELECTION" -> QuestionType.ONE_SELECTION
            "LISTEN_AND_ARRANGE_SENTENCE" -> QuestionType.LISTEN_AND_ARRANGE_SENTENCE
            "TRANSLATE_AND_ARRANGE_SENTENCE" -> QuestionType.TRANSLATE_AND_ARRANGE_SENTENCE
            "SPEAKING_ASSESSMENT" -> QuestionType.SPEAKING_ASSESSMENT
            else -> QuestionType.ONE_SELECTION
        },
        content = content,
        repeatable = repeatable,
        media = media?.toDomain(media),
        answers = answers.map { it.toDomain() }
    )
}

fun LessonSubmitResultDTO.toDomain(): LessonSubmitResult {
    return LessonSubmitResult(
        currentStreak = currentStreak,
        streakExtended = streakExtended
    )
}

fun VoiceAssessmentDTO.toDomain(): SpeakingAssessmentResult {
    return SpeakingAssessmentResult(
        recognitionStatus = recognitionStatus,
        displayText = displayText,
        audioUrl = audioUrl,
        overall = SpeakingAssessmentOverall(
            accuracyScore = overall.accuracyScore,
            fluencyScore = overall.fluencyScore,
            prosodyScore = overall.prosodyScore,
            completenessScore = overall.completenessScore,
            pronScore = overall.pronScore
        ),
        words = words.map {
            SpeakingAssessmentWord(
                word = it.word,
                accuracyScore = it.accuracyScore,
                errorType = it.errorType
            )
        }
    )
}

