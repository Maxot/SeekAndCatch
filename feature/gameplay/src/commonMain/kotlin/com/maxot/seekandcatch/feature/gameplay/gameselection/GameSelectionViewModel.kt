package com.maxot.seekandcatch.feature.gameplay.gameselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.gameplay.gameselection.model.GameSelectionUiEvent
import com.maxot.seekandcatch.feature.gameplay.gameselection.model.GameSelectionUiState
import com.maxot.seekandcatch.feature.settings.AudioController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameSelectionViewModel(
    private val settingsRepository: SettingsRepository,
    private val audioController: AudioController
) : ViewModel() {

    private val selectedGameDifficulty: StateFlow<GameDifficulty> =
        settingsRepository.observeDifficulty().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = GameDifficulty.NORMAL
        )

    private val selectedGameMode: StateFlow<GameMode> =
        settingsRepository.observeGameMode().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = GameMode.FLOW
        )

    val uiState: StateFlow<GameSelectionUiState> = combine(
        selectedGameDifficulty,
        selectedGameMode
    ) { difficulty, mode ->
        GameSelectionUiState(
            selectedDifficulty = difficulty,
            selectedGameMode = mode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = GameSelectionUiState()
    )

    fun onEvent(event: GameSelectionUiEvent) {
        when (event) {
            is GameSelectionUiEvent.ChangeGameDifficult -> setSelectedDifficulty(event.gameDifficult)
            is GameSelectionUiEvent.ChangeGameMode -> setSelectedGameMode(event.gameMode)
        }
    }

    private fun setSelectedDifficulty(gameDifficulty: GameDifficulty) {
        viewModelScope.launch {
            settingsRepository.setDifficulty(gameDifficulty)
            audioController.onButtonClick()
        }
    }

    private fun setSelectedGameMode(gameMode: GameMode) {
        viewModelScope.launch {
            settingsRepository.setGameMode(gameMode)
            audioController.onButtonClick()
        }
    }
}
