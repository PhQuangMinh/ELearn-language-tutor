package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.model.LessonInTopic
import com.nhom2.elearnlanguage.domain.repository.LessonRepository
import javax.inject.Inject

class GetLessonsByTopicUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    suspend operator fun invoke(topicId: Int): List<LessonInTopic> {
        return lessonRepository.getLessonsByTopic(topicId)
    }
}

