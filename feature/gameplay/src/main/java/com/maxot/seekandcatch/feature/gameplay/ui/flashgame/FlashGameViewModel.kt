package com.maxot.seekandcatch.feature.gameplay.ui.flashgame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.domain.flash.FlashGameEvent
import com.maxot.seekandcatch.core.domain.flash.FlashGameState
import com.maxot.seekandcatch.core.domain.flash.FlashGameUseCase
import com.maxot.seekandcatch.core.media.MusicManager
import com.maxot.seekandcatch.core.media.MusicType
import com.maxot.seekandcatch.core.media.SoundManager
import com.maxot.seekandcatch.core.media.SoundType
import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.data.model.GameMode
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.gameplay.ui.flashgame.model.FlashGameUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlashGameViewModel @Inject constructor(
    private val gameUseCase: FlashGameUseCase,
    private val settingsRepository: SettingsRepository,
    private val musicManager: MusicManager,
    private val soundManager: SoundManager,
) : ViewModel() {

    private val selectedGameDifficulty: StateFlow<GameDifficulty?> =
        settingsRepository.observeDifficulty().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )

    val selectedGameMode: StateFlow<GameMode> =
        settingsRepository.observeGameMode().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = GameMode.FLASH
        )


    private val _uiState = MutableStateFlow(FlashGameUiState())
    val uiState: StateFlow<FlashGameUiState> get() = _uiState

    init {
        observeGameState()
        launchGame()
    }

    private fun observeGameState() {
        viewModelScope.launch {
            gameUseCase.gameState.collect { state ->
                when (state) {
                    FlashGameState.Idle -> _uiState.update {
                        it.copy(
                            isLoading = true,
                            isReady = false,
                            isActive = false,
                            isPaused = false
                        )
                    }

                    is FlashGameState.Created -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            isReady = true,
                            isActive = false
                        )
                    }

                    FlashGameState.Started -> {}
                    is FlashGameState.Resumed -> {
                        val d = state.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isActive = true,
                                isPaused = false,
                                gridSize = d.gridSize,
                                gridWidth = kotlin.math.sqrt(d.gridSize.toDouble()).toInt()
                                    .coerceAtLeast(1),
                                visibleCells = d.visibleCells,
                                score = d.score,
                                lifeCount = d.lifeCount,
                                goals = d.goals,
                                goalSuitableFigures = d.goalSuitableFigures,
                                figuresByCell = d.figuresByCell,
                            )
                        }
                    }

                    FlashGameState.Paused -> _uiState.update {
                        it.copy(
                            isPaused = true,
                            isActive = false
                        )
                    }

                    is FlashGameState.Finished -> _uiState.update {
                        it.copy(
                            isActive = false,
                            isPaused = false,
                            isFinished = true
                        )
                    }
                }
            }
        }
    }

    private fun launchGame() {
        viewModelScope.launch {
            selectedGameDifficulty.collect { diff ->
                diff?.let {
                    gameUseCase.initGame(it.gameParams)
                    return@collect
                }
            }
        }
    }

    fun startGame() {
        musicManager.play(MusicType.GAME)
        gameUseCase.onEvent(FlashGameEvent.StartGame)
    }

    fun pauseGame() {
        gameUseCase.onEvent(FlashGameEvent.PauseGame)
        musicManager.pauseMusic()
        soundManager.playSound(SoundType.GAME_OVER)
    }

    fun resumeGame() {
        gameUseCase.onEvent(FlashGameEvent.ResumeGame)
        musicManager.resumeMusic()
    }

    fun finishGame() {
        musicManager.stopMusic()
        gameUseCase.onEvent(FlashGameEvent.FinishGame)
    }

    fun onCellClick(id: Int) {
        gameUseCase.onEvent(FlashGameEvent.OnCellClick(id))
    }
}