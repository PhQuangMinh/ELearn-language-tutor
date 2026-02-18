package com.nhom2.elearnlanguage.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.nhom2.elearnlanguage.domain.model.CourseProgress
import com.nhom2.elearnlanguage.domain.model.HomeData
import com.nhom2.elearnlanguage.domain.usecase.GetHomeDataUseCase
import com.nhom2.elearnlanguage.domain.usecase.GetMoreCoursesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeDataUseCase: GetHomeDataUseCase,
    private val getMoreCoursesUseCase: GetMoreCoursesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Paging state cho course list
    private var currentCoursePage = 0
    private val pageSize = 10
    private var isLoadingMoreCourses = false
    private var hasMoreCourses = true

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val data = getHomeDataUseCase()
                currentCoursePage = 0
                isLoadingMoreCourses = false
                hasMoreCourses = data.courses.size >= pageSize
                Log.d("HOME_PAGING", "init courses=${data.courses.size} hasMore=$hasMoreCourses")
                _uiState.value = HomeUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message)
            }
        }
    }

    fun loadMoreCourses() {
        val currentState = _uiState.value
        if (currentState !is HomeUiState.Success) return
        if (isLoadingMoreCourses || !hasMoreCourses) return

        viewModelScope.launch {
            try {
                isLoadingMoreCourses = true
                val nextPage = currentCoursePage + 1
                Log.d("HOME_PAGING", "loadMore page=$nextPage size=$pageSize")
                val newCourses: List<CourseProgress> = getMoreCoursesUseCase(nextPage, pageSize)
                Log.d("HOME_PAGING", "loadMore received=${newCourses.size}")

                if (newCourses.isNotEmpty()) {
                    val updatedCourses = currentState.data.courses + newCourses
                    val updatedData = currentState.data.copy(courses = updatedCourses)
                    _uiState.value = HomeUiState.Success(updatedData)
                    currentCoursePage = nextPage
                    hasMoreCourses = newCourses.size >= pageSize
                } else {
                    hasMoreCourses = false
                }
            } catch (e: Exception) {
                // Log để dễ debug (không tắt hasMore để còn retry khi user scroll tiếp)
                Log.e("HOME_PAGING", "loadMore failed: ${e.message}", e)
            } finally {
                isLoadingMoreCourses = false
            }
        }
    }
}

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val data: HomeData) : HomeUiState()
    data class Error(val message: String?) : HomeUiState()
}
