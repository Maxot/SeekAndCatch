package com.maxot.seekandcatch.feature.gameplay.ui.flowgame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.domain.FlowGameUseCase
import com.maxot.seekandcatch.core.domain.model.FlowGameEvent
import com.maxot.seekandcatch.core.domain.model.FlowGameState
import com.maxot.seekandcatch.core.media.MusicManager
import com.maxot.seekandcatch.core.media.MusicType
import com.maxot.seekandcatch.core.media.SoundManager
import com.maxot.seekandcatch.core.media.SoundType
import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.data.model.GameMode
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.gameplay.model.FlowGameUiEvent
import com.maxot.seekandcatch.feature.gameplay.ui.flowgame.model.FlowGameUiState
import com.maxot.seekandcatch.feature.settings.VibrationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlowGameViewModel
@Inject constructor(
    private val gameUseCase: FlowGameUseCase,
    private val settingsRepository: SettingsRepository,
    private val musicManager: MusicManager,
    private val vibrationManager: VibrationManager,
    private val soundManager: SoundManager,
) : ViewModel() {
    private var lastLifeCount: Int = 0
    private var lastCoefficient: Float = 0f

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
            initialValue = GameMode.FLOW
        )

    private val _flowGameUiState = MutableStateFlow(FlowGameUiState())
    val flowGameUiState: StateFlow<FlowGameUiState>
        get() = _flowGameUiState

    private val _readyToStart = MutableStateFlow(false)
    private val readyToStart: StateFlow<Boolean> = _readyToStart

    private var lifeWastedJob: Job? = null

    init {
        observeFlowGameState()
        launchGame()

        processReadyToStart()
    }

    fun onEvent(event: FlowGameUiEvent) {
        when (event) {
            is FlowGameUiEvent.OnItemClick -> onItemClick(event.itemId)
            FlowGameUiEvent.UpdateScrollDuration -> gameUseCase.onEvent(FlowGameEvent.UpdateScrollDuration)
            FlowGameUiEvent.UpdatePixelsToScroll -> gameUseCase.onEvent(FlowGameEvent.UpdatePixelsToScroll)
            FlowGameUiEvent.FinishGame -> finishGame()
            FlowGameUiEvent.PauseGame -> pauseGame()
            FlowGameUiEvent.ResumeGame -> resumeGame()
            FlowGameUiEvent.SetGameReadyToStart -> setGameReadyToStart()
            is FlowGameUiEvent.FirstVisibleItemIndexChanged -> onFirstVisibleItemIndexChanged(event.firstVisibleItemIndex)
            is FlowGameUiEvent.ItemHeightMeasured -> setItemHeight(event.height)
        }

    }

    private fun observeFlowGameState() {
        viewModelScope.launch {
            gameUseCase.gameState.collect { gameState ->
                when (gameState) {
                    FlowGameState.Idle -> {
                        _flowGameUiState.update {
                            it.copy(
                                isLoading = true,
                                isReady = false,
                                isActive = false,
                                isPaused = false
                            )
                        }
                    }

                    is FlowGameState.Created -> {
                        _flowGameUiState.update {
                            it.copy(
                                isLoading = false,
                                isReady = true,
                                isActive = false,
                                goalSuitableFigures = gameState.figuresSuitableForGoal
                            )
                        }
                    }

                    FlowGameState.Started -> {

                    }

                    FlowGameState.Paused -> {
                        _flowGameUiState.update {
                            it.copy(
                                isPaused = true,
                                isLoading = false,
                                isActive = false
                            )
                        }
                    }

                    is FlowGameState.Resumed -> {
                        _flowGameUiState.update {
                            it.copy(
                                lifeCount = gameState.data.lifeCount,
                                goals = gameState.data.goals,
                                goalSuitableFigures = gameState.data.goalSuitableFigures,
                                figures = gameState.data.figures,
                                score = gameState.data.score,
                                coefficient = gameState.data.coefficient,
                                gameDuration = gameState.data.gameDuration,
                                scrollDuration = gameState.data.scrollDuration,
                                pixelsToScroll = gameState.data.pixelsToScroll,
                                rowWidth = gameState.data.rowWidth,
                                isActive = true,
                                isLoading = false,
                                isPaused = false,
                            )
                        }
                        processLifeCountChanges(gameState.data.lifeCount)
                        processCoefficientChanges(gameState.data.coefficient)
                    }

                    is FlowGameState.Finished -> {
                        musicManager.stopMusic()
                        _flowGameUiState.update {
                            it.copy(
                                isReady = false,
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
            selectedGameDifficulty.collect {
                it?.let {
                    initGame(it)
                    this.cancel()
                }
            }
        }
    }

    private fun processReadyToStart() {
        viewModelScope.launch {
            readyToStart.collect { isReady ->
                if (isReady) {
                    startGame()
                    this.cancel()
                }
            }
        }
    }

    private fun setGameReadyToStart() {
        _readyToStart.value = true
    }

    private fun processLifeCountChanges(lifeCount: Int) {
        if (lastLifeCount > lifeCount)
            updateLifeWastedValue()
        lastLifeCount = lifeCount
    }

    private fun processCoefficientChanges(coefficient: Float) {
        if (lastCoefficient > coefficient)
            updateLifeWastedValue()
        lastCoefficient = coefficient
    }


    private fun updateLifeWastedValue() {
        _flowGameUiState.value = flowGameUiState.value.copy(isLifeWasted = false)
        lifeWastedJob?.cancel()
        lifeWastedJob = viewModelScope.launch {
            vibrationManager.vibrate()
            delay(100)
            _flowGameUiState.value = flowGameUiState.value.copy(isLifeWasted = true)
            delay(1000)
            _flowGameUiState.value = flowGameUiState.value.copy(isLifeWasted = false)
        }
    }


    private fun initGame(gameDifficulty: GameDifficulty) {
        soundManager.playSound(SoundType.COUNTDOWN)
        musicManager.stopMusic()
        gameUseCase.initGame(gameDifficulty.gameParams)
    }

    private fun startGame() {
        musicManager.play(MusicType.GAME)
        gameUseCase.onEvent(FlowGameEvent.StartGame)
    }

    private fun pauseGame() {
        if (readyToStart.value) {
            musicManager.pauseMusic()
            gameUseCase.onEvent(FlowGameEvent.PauseGame)
        }
    }

    private fun resumeGame() {
        if (readyToStart.value) {
            musicManager.resumeMusic()
            gameUseCase.onEvent(FlowGameEvent.ResumeGame)
        }
    }

    private fun finishGame() {
        soundManager.playSound(SoundType.GAME_OVER)
        gameUseCase.onEvent(FlowGameEvent.FinishGame)
    }

    private fun onItemClick(id: Int) {
        viewModelScope.launch {
            soundManager.playSound(SoundType.FIGURE_CLICK)
            gameUseCase.onEvent(FlowGameEvent.OnItemClick(id))
        }
    }

    private fun onFirstVisibleItemIndexChanged(index: Int) {
        gameUseCase.onEvent(FlowGameEvent.FirstVisibleItemIndexChanged(index))
    }

    private fun setItemHeight(height: Int) =
        gameUseCase.onEvent(FlowGameEvent.ItemHeightMeasured(height))

    override fun onCleared() {
        super.onCleared()
        musicManager.releaseMusic()
    }
}
