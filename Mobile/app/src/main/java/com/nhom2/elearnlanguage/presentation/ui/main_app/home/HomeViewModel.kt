package com.nhom2.elearnlanguage.presentation.ui.main_app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.nhom2.elearnlanguage.domain.model.CourseProgress
import com.nhom2.elearnlanguage.domain.model.HomeData
import com.nhom2.elearnlanguage.domain.usecase.GetHomeDataUseCase
import com.nhom2.elearnlanguage.domain.usecase.GetMoreCoursesUseCase
import com.nhom2.elearnlanguage.domain.usecase.GetUserStreakUseCase
import com.nhom2.elearnlanguage.domain.repository.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeDataUseCase: GetHomeDataUseCase,
    private val getMoreCoursesUseCase: GetMoreCoursesUseCase,
    private val getUserStreakUseCase: GetUserStreakUseCase,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val _currentStreak = MutableStateFlow<Int?>(null)
    val currentStreak: StateFlow<Int?> = _currentStreak.asStateFlow()
    private val _streakStatus = MutableStateFlow(StreakStatus.UNKNOWN)
    val streakStatus: StateFlow<StreakStatus> = _streakStatus.asStateFlow()
    private val _isLoadingMoreCourses = MutableStateFlow(false)
    val isLoadingMoreCourses: StateFlow<Boolean> = _isLoadingMoreCourses.asStateFlow()
    private var isLoadingStreak = false
    private var lastStreakLoadStartedAtMs = 0L
    private val streakLoadMinIntervalMs = 800L

    // Paging state cho course list
    private var currentCoursePage = 0
    private val pageSize = 10
    private var isLoadingMoreCoursesInFlight = false
    private var hasMoreCourses = true

    init {
        loadHomeData()
        loadCurrentStreak()
    }

    fun refreshCurrentStreak() {
        loadCurrentStreak()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val data = getHomeDataUseCase()
                currentCoursePage = 0
                isLoadingMoreCoursesInFlight = false
                _isLoadingMoreCourses.value = false
                hasMoreCourses = data.courses.size >= pageSize
                Log.d("HOME_PAGING", "init courses=${data.courses.size} hasMore=$hasMoreCourses")
                _uiState.value = HomeUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message)
            }
        }
    }

    private fun loadCurrentStreak() {
        val now = System.currentTimeMillis()
        if (isLoadingStreak) {
            Log.d("HOME_STREAK", "Skip loadCurrentStreak: already loading")
            return
        }
        if (now - lastStreakLoadStartedAtMs < streakLoadMinIntervalMs) {
            Log.d("HOME_STREAK", "Skip loadCurrentStreak: too frequent")
            return
        }

        isLoadingStreak = true
        lastStreakLoadStartedAtMs = now
        viewModelScope.launch {
            try {
                val userId = tokenStorage.getUserId()
                if (userId == null) {
                    Log.w("HOME_STREAK", "Missing user id, skip getUserStreak")
                    _streakStatus.value = StreakStatus.INACTIVE
                    return@launch
                }
                val userStreak = getUserStreakUseCase(userId)
                _currentStreak.value = userStreak.currentStreak
                _streakStatus.value = if (isStreakActiveToday(userStreak.lastStreakUpdated)) {
                    StreakStatus.ACTIVE
                } else {
                    StreakStatus.INACTIVE
                }
            } catch (e: Exception) {
                Log.e("HOME_STREAK", "loadCurrentStreak failed: ${e.message}", e)
                _streakStatus.value = StreakStatus.INACTIVE
            } finally {
                isLoadingStreak = false
            }
        }
    }

    private fun isStreakActiveToday(lastStreakUpdated: java.time.LocalDateTime?): Boolean {
        val updatedDate = lastStreakUpdated?.toLocalDate() ?: return false
        return updatedDate == LocalDate.now()
    }

    fun loadMoreCourses() {
        val currentState = _uiState.value
        if (currentState !is HomeUiState.Success) return
        if (isLoadingMoreCoursesInFlight || !hasMoreCourses) return

        viewModelScope.launch {
            try {
                isLoadingMoreCoursesInFlight = true
                _isLoadingMoreCourses.value = true
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
                isLoadingMoreCoursesInFlight = false
                _isLoadingMoreCourses.value = false
            }
        }
    }

    fun canLoadMoreCourses(): Boolean = hasMoreCourses
}

enum class StreakStatus {
    ACTIVE,
    INACTIVE,
    UNKNOWN
}

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val data: HomeData) : HomeUiState()
    data class Error(val message: String?) : HomeUiState()
}
