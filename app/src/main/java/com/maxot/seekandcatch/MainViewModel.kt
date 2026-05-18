package com.maxot.seekandcatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.common.VisualFeedbackManager
import com.maxot.seekandcatch.core.domain.AuthUseCase
import com.maxot.seekandcatch.core.model.UserConfig
import com.maxot.seekandcatch.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val settingsRepository: SettingsRepository,
    private val authUseCase: AuthUseCase,
    val visualFeedbackManager: VisualFeedbackManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            authUseCase.autoRegisterIfNeeded()
        }
    }

    val uiState: StateFlow<MainActivityUiState> =
        settingsRepository.userConfig.map {
            MainActivityUiState.Success(it)
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
