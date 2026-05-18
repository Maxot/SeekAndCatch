package com.maxot.seekandcatch.feature.gameplay.ui.flashgame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.common.VisualFeedbackManager
import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode
import com.maxot.seekandcatch.core.domain.flash.FlashGameEvent
import com.maxot.seekandcatch.core.domain.flash.FlashGameState
import com.maxot.seekandcatch.core.domain.flash.FlashGameUseCase
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.gameplay.ui.flashgame.model.FlashGameUiState
import com.maxot.seekandcatch.feature.settings.AudioController
import com.maxot.seekandcatch.feature.settings.HapticsController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FlashGameViewModel(
    private val gameUseCase: FlashGameUseCase,
    private val settingsRepository: SettingsRepository,
    private val hapticsController: HapticsController,
    private val visualFeedbackManager: VisualFeedbackManager,
    private val audioController: AudioController,
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
                        it.copy(isLoading = true, isReady = false, isActive = false, isPaused = false)
                    }
                    is FlashGameState.Created -> _uiState.update {
                        it.copy(isLoading = false, isReady = true, isActive = false, goalSuitableFigures = state.figuresSuitableForGoal)
                    }
                    FlashGameState.Started -> {}
                    is FlashGameState.Resumed -> {
                        val d = state.data
                        _uiState.update {
                            it.copy(
                                isLoading = false, isActive = true, isPaused = false,
                                gridSize = d.gridSize, gridWidth = d.gridWidth,
                                visibleCells = d.visibleCells, score = d.score,
                                lifeCount = d.lifeCount, coefficient = d.coefficient,
                                gameDuration = d.gameDuration, goals = d.goals,
                                goalSuitableFigures = d.goalSuitableFigures,
                                figuresByCell = d.figuresByCell,
                                isLifeWasted = d.isLifeWasted || it.isLifeWasted
                            )
                        }
                        processLifeCountChanges(state.data.lifeCount)
                        processCoefficientChanges(state.data.coefficient)
                    }
                    FlashGameState.Paused -> _uiState.update {
                        it.copy(isPaused = true, isActive = false)
                    }
                    is FlashGameState.Finished -> {
                        viewModelScope.launch { hapticsController.vibrateError() }
                        audioController.onGameOver()
                        val lastData = state.lastData
                        _uiState.update { currentState ->
                            val base = if (lastData != null) {
                                currentState.copy(
                                    gridSize = lastData.gridSize, gridWidth = lastData.gridWidth,
                                    visibleCells = lastData.visibleCells, score = lastData.score,
                                    lifeCount = lastData.lifeCount, coefficient = lastData.coefficient,
                                    gameDuration = lastData.gameDuration, goals = lastData.goals,
                                    goalSuitableFigures = lastData.goalSuitableFigures,
                                    figuresByCell = lastData.figuresByCell,
                                )
                            } else currentState
                            base.copy(isActive = false, isPaused = false, isFinished = true)
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
                    audioController.onGameStart()
                    gameUseCase.initGame(it.gameParams)
                    return@collect
                }
            }
        }
    }

    fun startGame() {
        audioController.onGameplayStarted()
        gameUseCase.onEvent(FlashGameEvent.StartGame)
    }

    fun pauseGame() {
        gameUseCase.onEvent(FlashGameEvent.PauseGame)
        audioController.onGamePaused()
    }

    fun resumeGame() {
        gameUseCase.onEvent(FlashGameEvent.ResumeGame)
        audioController.onGameResumed()
    }

    fun finishGame() {
        audioController.onGameOver()
        gameUseCase.onEvent(FlashGameEvent.FinishGame)
    }

    private fun processLifeCountChanges(lifeCount: Int) {
        if (lastLifeCount > lifeCount) {
            updateLifeWastedValue()
            audioController.onMiss()
        }
        lastLifeCount = lifeCount
    }

    private fun processCoefficientChanges(coefficient: Float) {
        if (lastCoefficient > coefficient) {
            updateLifeWastedValue()
            audioController.onMiss()
        }
        lastCoefficient = coefficient
    }

    private fun updateLifeWastedValue() {
        viewModelScope.launch { hapticsController.vibrateError() }
        visualFeedbackManager.triggerLifeWasted(viewModelScope)
    }

    fun onCellClick(id: Int) {
        viewModelScope.launch { hapticsController.vibrateCorrect() }
        audioController.onCorrectTap()
        gameUseCase.onEvent(FlashGameEvent.OnCellClick(id))
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSoundState(enabled) }
    }

    fun toggleMusic(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setMusicState(enabled) }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setVibrationState(enabled) }
    }

    override fun onCleared() {
        gameUseCase.onEvent(FlashGameEvent.ResetGame)
        super.onCleared()
        audioController.release()
    }
}
