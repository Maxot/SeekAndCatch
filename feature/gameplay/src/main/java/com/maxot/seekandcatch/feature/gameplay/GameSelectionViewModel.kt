package com.maxot.seekandcatch.feature.gameplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.data.model.GameMode
import com.maxot.seekandcatch.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameSelectionViewModel
@Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val selectedGameDifficulty: StateFlow<GameDifficulty?> =
        settingsRepository.observeDifficulty().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )

    val selectedGameMode: StateFlow<GameMode?> =
        settingsRepository.observeGameMode().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun setSelectedDifficulty(gameDifficulty: GameDifficulty) {
        viewModelScope.launch {
            settingsRepository.setDifficulty(gameDifficulty)
        }
    }

    fun setSelectedGameMode(gameMode: GameMode) {
        viewModelScope.launch {
            settingsRepository.setGameMode(gameMode)
        }
    }
}
