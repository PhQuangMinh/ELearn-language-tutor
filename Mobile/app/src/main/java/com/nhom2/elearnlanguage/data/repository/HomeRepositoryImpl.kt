package com.nhom2.elearnlanguage.data.repository

import android.util.Log
import com.nhom2.elearnlanguage.data.mapper.toDomain
import com.nhom2.elearnlanguage.data.source.remote.HomeDataSource
import com.nhom2.elearnlanguage.domain.model.CourseProgress
import com.nhom2.elearnlanguage.domain.model.HomeData
import com.nhom2.elearnlanguage.domain.repository.HomeRepository
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val homeDataSource: HomeDataSource
) : HomeRepository {

    override suspend fun getHomeData(): HomeData {
        val response = homeDataSource.getHomeData()
        if (!response.success || response.data == null) {
            Log.e("HOME_API", "getHomeData failed: ${response.message}")
            throw Exception(response.message)
        }
        return response.data!!.toDomain()
    }

    override suspend fun getCourses(page: Int, size: Int): List<CourseProgress> {
        val response = homeDataSource.getCourses(page, size)
        if (!response.success || response.data == null) {
            Log.e("HOME_API", "getCourses failed: ${response.message}")
            throw Exception(response.message)
        }
        return response.data!!.map { it.toDomain() }
    }
}
