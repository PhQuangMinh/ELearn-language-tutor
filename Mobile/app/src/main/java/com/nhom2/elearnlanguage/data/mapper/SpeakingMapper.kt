package com.nhom2.elearnlanguage.data.mapper

import com.nhom2.elearnlanguage.data.dto.AiRespondResponseDTO
import com.nhom2.elearnlanguage.data.dto.ScenarioDetailResponseDTO
import com.nhom2.elearnlanguage.domain.model.speaking.AiRespondResult
import com.nhom2.elearnlanguage.domain.model.speaking.AiUserHints
import com.nhom2.elearnlanguage.domain.model.speaking.LessonScenario

fun ScenarioDetailResponseDTO.toDomain(): LessonScenario {
    return LessonScenario(
        id = id,
        title = title,
        description = description,
        tasks = tasks,
        openingMessage = openningMessage,
        suggestion = suggestion,
        translation = translation
    )
}

fun AiRespondResponseDTO.toDomain(): AiRespondResult {
    return AiRespondResult(
        aiMessage = ai_message,
        aiMessageTranslation = ai_message_translation,
        userHints = AiUserHints(
            analysis = user_hints.analysis,
            suggestion = user_hints.suggestion,
            example = user_hints.example
        )
    )
}

