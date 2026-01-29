package com.maxot.seekandcatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.common.VisualFeedbackManager
import com.maxot.seekandcatch.core.model.UserConfig
import com.maxot.seekandcatch.data.repository.AuthRepository
import com.maxot.seekandcatch.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    val visualFeedbackManager: VisualFeedbackManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            authRepository.autoRegisterIfNeeded()
        }
    }

    val uiState: StateFlow<MainActivityUiState> =
        settingsRepository.userConfig.map {
            MainActivityUiState.Success(UserConfig(darkThemeConfig = it.darkThemeConfig))
        }.stateIn(
            viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = MainActivityUiState.Loading
        )
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(val userConfig: UserConfig) : MainActivityUiState
}
