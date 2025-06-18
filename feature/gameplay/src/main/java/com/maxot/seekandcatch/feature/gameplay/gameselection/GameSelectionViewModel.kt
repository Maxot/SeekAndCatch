package com.maxot.seekandcatch.feature.gameplay.gameselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.media.SoundManager
import com.maxot.seekandcatch.core.media.SoundType
import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.data.model.GameMode
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.gameplay.gameselection.model.GameSelectionUiEvent
import com.maxot.seekandcatch.feature.gameplay.gameselection.model.GameSelectionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameSelectionViewModel
@Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val soundManager: SoundManager
) : ViewModel() {
    private val selectedGameDifficulty: StateFlow<GameDifficulty> =
        settingsRepository.observeDifficulty().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.Lazily,
            initialValue = GameDifficulty.NORMAL
        )

    private val selectedGameMode: StateFlow<GameMode> =
        settingsRepository.observeGameMode().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.Eagerly,
            initialValue = GameMode.FLOW
        )

    private val _uiState: StateFlow<GameSelectionUiState> = combine(
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

    val uiState: StateFlow<GameSelectionUiState> = _uiState

    fun onEvent(event: GameSelectionUiEvent) {
        when (event) {

            is GameSelectionUiEvent.ChangeGameDifficult -> {
                setSelectedDifficulty(event.gameDifficult)
            }

            is GameSelectionUiEvent.ChangeGameMode -> {
                setSelectedGameMode(event.gameMode)
            }

        }
    }


    private fun setSelectedDifficulty(gameDifficulty: GameDifficulty) {
        viewModelScope.launch {
            settingsRepository.setDifficulty(gameDifficulty)
            soundManager.playSound(SoundType.BUTTON_CLICK)
        }
    }

    private fun setSelectedGameMode(gameMode: GameMode) {
        viewModelScope.launch {
            settingsRepository.setGameMode(gameMode)
            soundManager.playSound(SoundType.BUTTON_CLICK)
        }
    }
}
