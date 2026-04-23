package com.nhom2.elearnlanguage.domain.usecase

import com.nhom2.elearnlanguage.domain.model.CourseProgress
import com.nhom2.elearnlanguage.domain.repository.HomeRepository
import javax.inject.Inject

class GetMoreCoursesUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(page: Int, size: Int): List<CourseProgress> {
        return homeRepository.getCourses(page, size)
    }
}

