package com.nhom2.elearnlanguage.presentation.ui.main_app.home.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhom2.elearnlanguage.domain.model.UserProfile
import com.nhom2.elearnlanguage.domain.repository.AuthRepository
import com.nhom2.elearnlanguage.domain.repository.ProfileRepository
import com.nhom2.elearnlanguage.domain.repository.TokenStorage
import com.nhom2.elearnlanguage.domain.usecase.GetUserStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isLoggingOut: Boolean = false,
    val profile: UserProfile? = null,
    val currentStreak: Int? = null,
    val longestStreak: Int? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val logoutCompleted: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val getUserStreakUseCase: GetUserStreakUseCase,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var hasLoadedProfile = false

    fun loadProfile(forceReload: Boolean = false) {
        if (hasLoadedProfile && !forceReload) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
            try {
                val profile = profileRepository.getMyProfile()
                val userId = tokenStorage.getUserId()
                val streak = try {
                    userId?.let { getUserStreakUseCase(it) }
                } catch (_: Exception) {
                    null
                }
                hasLoadedProfile = true
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    profile = profile,
                    currentStreak = streak?.currentStreak ?: _uiState.value.currentStreak,
                    longestStreak = streak?.longestStreak ?: _uiState.value.longestStreak,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun updateProfile(
        fullName: String,
        avatarBytes: ByteArray?,
        avatarFileName: String?,
        avatarMimeType: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )
            try {
                val updated = profileRepository.updateMyProfile(
                    fullName = fullName,
                    avatarBytes = avatarBytes,
                    avatarFileName = avatarFileName,
                    avatarMimeType = avatarMimeType
                )
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    profile = updated,
                    successMessage = "Cập nhật hồ sơ người dùng thành công!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun consumeMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoggingOut = true,
                errorMessage = null,
                successMessage = null,
                logoutCompleted = false
            )

            val refreshToken = tokenStorage.getRefreshToken()
            val logoutErrorMessage = try {
                if (!refreshToken.isNullOrBlank()) {
                    authRepository.logout(refreshToken)
                }
                null
            } catch (e: Exception) {
                e.message
            } finally {
                tokenStorage.clearTokens()
            }

            _uiState.value = _uiState.value.copy(
                isLoggingOut = false,
                logoutCompleted = true,
                errorMessage = logoutErrorMessage
            )
        }
    }

    fun consumeLogoutEvent() {
        _uiState.value = _uiState.value.copy(logoutCompleted = false)
    }
}
