package com.nhom2.elearnlanguage.domain.repository

import com.nhom2.elearnlanguage.domain.model.CourseProgress
import com.nhom2.elearnlanguage.domain.model.HomeData

interface HomeRepository {
    suspend fun getHomeData(): HomeData
    suspend fun getCourses(page: Int, size: Int): List<CourseProgress>
}
