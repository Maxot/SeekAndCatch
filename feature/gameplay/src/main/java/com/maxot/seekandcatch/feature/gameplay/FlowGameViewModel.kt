package com.maxot.seekandcatch.feature.gameplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.domain.FlowGameUseCase
import com.maxot.seekandcatch.core.domain.GameState
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.settings.AppSoundManager
import com.maxot.seekandcatch.feature.settings.VibrationManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val appSoundManager: AppSoundManager,
    private val vibrationManager: VibrationManager
) : ViewModel() {
    val goals: StateFlow<Set<Goal<Any>>> = gameUseCase.goals
    val score: StateFlow<Int> = gameUseCase.score
    val figures: StateFlow<List<Figure>> = gameUseCase.figures
    val coefficient: StateFlow<Float> = gameUseCase.coefficient
    val gameDuration: StateFlow<Long> = gameUseCase.gameDuration
    val selectedGameDifficulty: StateFlow<GameDifficulty?> =
        settingsRepository.observeDifficulty().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
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

    init {
        viewModelScope.launch {
            appSoundManager.init()

            coefficient.collect {
                val musicSpeed = 1 + it / 5
                appSoundManager.setMusicSpeed(musicSpeed.toFloat())
            }
        }
        processCoefficientChanges()
        viewModelScope.launch {
            selectedGameDifficulty.collect {
                it?.let {
                    initGame(it)
                    startGame()
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
        appSoundManager.startMusic()
        gameUseCase.startGame()
    }

    fun pauseGame() {
        appSoundManager.pauseMusic()
        gameUseCase.pauseGame()
    }

    fun resumeGame() {
        appSoundManager.startMusic()
        gameUseCase.resumeGame()
    }

    fun finishGame() {
        appSoundManager.stopMusic()
        gameUseCase.finishGame()
    }

    private fun stopMusic() {
        appSoundManager.stopMusic()
    }

    fun onItemClick(id: Int) {
        gameUseCase.onItemClick(id)
    }

    fun getRowWidth() = gameUseCase.getRowWidth()

    fun getPixelsToScroll(): Float = gameUseCase.getPixelsToScroll()

    fun getScrollDuration(): Int = gameUseCase.getScrollDuration()

    fun onFirstVisibleItemIndexChanged(index: Int) {
        gameUseCase.setFirstVisibleItemIndex(index)
    }

    fun setItemHeight(height: Int) = gameUseCase.setItemHeight(height)

    override fun onCleared() {
        super.onCleared()
        appSoundManager.release()
    }
}

sealed interface FlowGameUiState {
    data object Active : FlowGameUiState
    data object Paused : FlowGameUiState
    data object Loading : FlowGameUiState
    data object Finished : FlowGameUiState
}
