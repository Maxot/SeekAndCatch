package com.maxot.seekandcatch.feature.gameplay.ui.flashgame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.common.VisualFeedbackManager
import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode
import com.maxot.seekandcatch.core.domain.flash.FlashGameEvent
import com.maxot.seekandcatch.core.domain.flash.FlashGameState
import com.maxot.seekandcatch.core.domain.flash.FlashGameUseCase
import com.maxot.seekandcatch.core.media.AudioManager
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.gameplay.ui.flashgame.model.FlashGameUiState
import com.maxot.seekandcatch.feature.settings.VibrationManager
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
    private val vibrationManager: VibrationManager,
    private val visualFeedbackManager: VisualFeedbackManager,
    private val audioManager: AudioManager,
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

    private var lastLifeCount = 0
    private var lastCoefficient = 1f

    init {
        observeGameState()
        launchGame()

        viewModelScope.launch {
            visualFeedbackManager.isLifeWasted.collect { isWasted ->
                _uiState.update { it.copy(isLifeWasted = isWasted) }
            }
        }

        viewModelScope.launch {
            settingsRepository.observeSoundState().collect { enabled ->
                _uiState.update { it.copy(isSoundEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.observeMusicState().collect { enabled ->
                _uiState.update { it.copy(isMusicEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.observeVibrationState().collect { enabled ->
                _uiState.update { it.copy(isVibrationEnabled = enabled) }
            }
        }
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
                            isActive = false,
                            // Ensure goals info is visible before the game starts
                            goalSuitableFigures = state.figuresSuitableForGoal
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
                                coefficient = d.coefficient,
                                gameDuration = d.gameDuration,
                                goals = d.goals,
                                goalSuitableFigures = d.goalSuitableFigures,
                                figuresByCell = d.figuresByCell,
                                isLifeWasted = d.isLifeWasted || it.isLifeWasted
                            )
                        }
                        processLifeCountChanges(state.data.lifeCount)
                        processCoefficientChanges(state.data.coefficient)
                    }

                    FlashGameState.Paused -> _uiState.update {
                        it.copy(
                            isPaused = true,
                            isActive = false
                        )
                    }

                    is FlashGameState.Finished -> {
                        viewModelScope.launch {
                            vibrationManager.vibrateError()
                        }
                        audioManager.onGameOver()
                        val lastData = state.lastData
                        _uiState.update { currentState ->
                            val base = if (lastData != null) {
                                currentState.copy(
                                    gridSize = lastData.gridSize,
                                    gridWidth = kotlin.math.sqrt(lastData.gridSize.toDouble()).toInt()
                                        .coerceAtLeast(1),
                                    visibleCells = lastData.visibleCells,
                                    score = lastData.score,
                                    lifeCount = lastData.lifeCount,
                                    coefficient = lastData.coefficient,
                                    gameDuration = lastData.gameDuration,
                                    goals = lastData.goals,
                                    goalSuitableFigures = lastData.goalSuitableFigures,
                                    figuresByCell = lastData.figuresByCell,
                                )
                            } else currentState

                            base.copy(
                                isActive = false,
                                isPaused = false,
                                isFinished = true
                            )
                        }
                    }
                }
            }
        }
    }

    private fun launchGame() {
        viewModelScope.launch {
            selectedGameDifficulty.collect { diff ->
                diff?.let {
                    _uiState.update { it.copy(isFinished = false, score = 0) }
                    audioManager.onGameStart()
                    gameUseCase.initGame(it.gameParams)
                    return@collect
                }
            }
        }
    }

    fun startGame() {
        audioManager.onGameplayStarted()
        gameUseCase.onEvent(FlashGameEvent.StartGame)
    }

    fun pauseGame() {
        gameUseCase.onEvent(FlashGameEvent.PauseGame)
        audioManager.onGamePaused()
    }

    fun resumeGame() {
        gameUseCase.onEvent(FlashGameEvent.ResumeGame)
        audioManager.onGameResumed()
    }

    fun finishGame() {
        audioManager.onGameOver()
        gameUseCase.onEvent(FlashGameEvent.FinishGame)
    }

    private fun processLifeCountChanges(lifeCount: Int) {
        if (lastLifeCount > lifeCount) {
            updateLifeWastedValue()
            audioManager.onMiss()
        }
        lastLifeCount = lifeCount
    }

    private fun processCoefficientChanges(coefficient: Float) {
        if (lastCoefficient > coefficient) {
            updateLifeWastedValue()
            audioManager.onMiss()
        }
        lastCoefficient = coefficient
    }


    private fun updateLifeWastedValue() {
        viewModelScope.launch {
            vibrationManager.vibrateError()
        }
        visualFeedbackManager.triggerLifeWasted(viewModelScope)
    }

    fun onCellClick(id: Int) {
        viewModelScope.launch {
            vibrationManager.vibrateCorrect()
        }
        audioManager.onCorrectTap()
        gameUseCase.onEvent(FlashGameEvent.OnCellClick(id))
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundState(enabled)
        }
    }

    fun toggleMusic(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMusicState(enabled)
        }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVibrationState(enabled)
        }
    }

    override fun onCleared() {
        gameUseCase.onEvent(FlashGameEvent.ResetGame)
        super.onCleared()
        audioManager.release()
    }
}