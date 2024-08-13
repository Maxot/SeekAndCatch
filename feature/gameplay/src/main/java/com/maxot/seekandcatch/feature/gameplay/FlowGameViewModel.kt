package com.maxot.seekandcatch.feature.gameplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.domain.FlowGameUseCase
import com.maxot.seekandcatch.data.model.GameMode
import com.maxot.seekandcatch.core.domain.GameState
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.settings.MusicManager
import com.maxot.seekandcatch.feature.settings.VibrationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlowGameViewModel
@Inject constructor(
    private val gameUseCase: FlowGameUseCase,
    private val settingsRepository: SettingsRepository,
    private val musicManager: MusicManager,
    private val vibrationManager: VibrationManager
) : ViewModel() {
    val lifeCount: StateFlow<Int> = gameUseCase.lifeCount
    val goals: StateFlow<Set<Goal<Any>>> = gameUseCase.goals
    val score: StateFlow<Int> = gameUseCase.score
    val figures: StateFlow<List<Figure>> = gameUseCase.figures
    val coefficient: StateFlow<Float> = gameUseCase.coefficient
    val gameDuration: StateFlow<Long> = gameUseCase.gameDuration
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

    val flowGameUiState: StateFlow<FlowGameUiState> =
        gameUseCase.gameState.map {
            when (it) {
                GameState.IDLE, GameState.CREATED -> {
                    FlowGameUiState.Loading
                }

                GameState.STARTED, GameState.RESUMED -> {
                    FlowGameUiState.Active
                }

                GameState.PAUSED -> {
                    FlowGameUiState.Paused
                }

                GameState.FINISHED -> {
                    stopMusic() // ????
                    FlowGameUiState.Finished
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = FlowGameUiState.Loading
        )

    private val _readyToStart = MutableStateFlow(false)
    val readyToStart: StateFlow<Boolean> = _readyToStart

    init {
        launchGame()

        processReadyToStart()
        processLifeCountChanges()
        processCoefficientChanges()
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

    fun setGameReadyToStart() {
        _readyToStart.value = true
    }

    private fun processLifeCountChanges() {
        var lastLifeCount = lifeCount.value
        viewModelScope.launch {
            lifeCount.collect {
                if (lastLifeCount > it) {
                    vibrationManager.vibrate()
                    lastLifeCount = it
                }
            }
        }
    }

    private fun processCoefficientChanges() {
        var lastCoefficient = coefficient.value
        viewModelScope.launch {
            coefficient.collect {
                if (it < lastCoefficient) vibrationManager.vibrate()
                lastCoefficient = coefficient.value
            }
        }
    }

    private fun initGame(gameDifficulty: GameDifficulty) {
        gameUseCase.initGame(gameDifficulty.gameParams)
    }

    private fun startGame() {
        musicManager.startMusic()
        gameUseCase.startGame()
    }

    fun pauseGame() {
        if (readyToStart.value) {
            musicManager.pauseMusic()
            gameUseCase.pauseGame()
        }
    }

    fun resumeGame() {
        if (readyToStart.value) {
            musicManager.startMusic()
            gameUseCase.resumeGame()
        }
    }

    fun finishGame() {
        musicManager.stopMusic()
        gameUseCase.finishGame()
    }

    private fun stopMusic() {
        musicManager.stopMusic()
    }

    fun onItemClick(id: Int): Int {
        return gameUseCase.onItemClick(id)
    }

    fun getMaxLifeCount() = gameUseCase.maxLifeCount

    fun getRowWidth() = gameUseCase.getRowWidth()

    fun getPixelsToScroll(): Float = gameUseCase.getPixelsToScroll()

    fun getScrollDuration(): Int = gameUseCase.getScrollDuration()

    fun onFirstVisibleItemIndexChanged(index: Int) {
        gameUseCase.setFirstVisibleItemIndex(index)
    }

    fun setItemHeight(height: Int) = gameUseCase.setItemHeight(height)

    override fun onCleared() {
        super.onCleared()
        musicManager.release()
    }
}

sealed interface FlowGameUiState {
    data object Active : FlowGameUiState
    data object Paused : FlowGameUiState
    data object Loading : FlowGameUiState
    data object Finished : FlowGameUiState
}
