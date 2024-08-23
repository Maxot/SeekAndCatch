package com.maxot.seekandcatch.feature.gameplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.domain.FlowGameUseCase
import com.maxot.seekandcatch.core.domain.model.FlowGameEvent
import com.maxot.seekandcatch.core.domain.model.FlowGameState
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.data.model.GameMode
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.gameplay.model.FlowGameUiCallback
import com.maxot.seekandcatch.feature.gameplay.model.FlowGameUiEvent
import com.maxot.seekandcatch.feature.settings.MusicManager
import com.maxot.seekandcatch.feature.settings.VibrationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
    private val vibrationManager: VibrationManager
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
            initialValue = GameMode.FLOW
        )

    private val _flowGameUiState = MutableStateFlow(FlowGameUiState())
    val flowGameUiState: StateFlow<FlowGameUiState>
        get() = _flowGameUiState

    private val _readyToStart = MutableStateFlow(false)
    val readyToStart: StateFlow<Boolean> = _readyToStart

    private val _uiCallback = Channel<FlowGameUiCallback>()
    val uiCallback = _uiCallback.receiveAsFlow()

    init {
        observeFlowGameState()
        launchGame()

        processReadyToStart()
        processLifeCountChanges()
        processCoefficientChanges()
    }

    fun onEvent(event: FlowGameUiEvent) {
        when (event) {
            is FlowGameUiEvent.OnItemClick -> viewModelScope.launch { onItemClick(event.itemId) }
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

                    }

                    FlowGameState.Created -> {
                        _flowGameUiState.update {
                            it.copy(
                                isLoading = true,
                                isActive = false
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
                                figures = gameState.data.figures,
                                score = gameState.data.score,
                                coefficient = gameState.data.coefficient,
                                gameDuration = gameState.data.gameDuration,
                                scrollDuration = gameState.data.scrollDuration,
                                pixelsToScroll = gameState.data.pixelsToScroll,
                                isActive = true,
                                isLoading = false,
                                isPaused = false,
                                )
                        }
                    }

                    is FlowGameState.Finished -> {
                        _flowGameUiState.update {
                            it.copy(
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

    private fun processLifeCountChanges() {
//        var lastLifeCount = lifeCount.value
//        viewModelScope.launch {
//            lifeCount.collect {
//                if (lastLifeCount > it) {
//                    vibrationManager.vibrate()
//                    lastLifeCount = it
//                }
//            }
//        }
    }

    private fun processCoefficientChanges() {
//        var lastCoefficient = coefficient.value
//        viewModelScope.launch {
//            coefficient.collect {
//                if (it < lastCoefficient) vibrationManager.vibrate()
//                lastCoefficient = coefficient.value
//            }
//        }
    }

    private fun initGame(gameDifficulty: GameDifficulty) {
        gameUseCase.initGame(gameDifficulty.gameParams)
    }

    private fun startGame() {
        musicManager.startMusic()
//        gameUseCase.startGame()
        gameUseCase.onEvent(FlowGameEvent.StartGame)
    }

    private fun pauseGame() {
        if (readyToStart.value) {
            musicManager.pauseMusic()
//            gameUseCase.pauseGame()
            gameUseCase.onEvent(FlowGameEvent.PauseGame)
        }
    }

    private fun resumeGame() {
        if (readyToStart.value) {
            musicManager.startMusic()
//            gameUseCase.resumeGame()
            gameUseCase.onEvent(FlowGameEvent.ResumeGame)
        }
    }

    private fun finishGame() {
        musicManager.stopMusic()
//        gameUseCase.finishGame()
        gameUseCase.onEvent(FlowGameEvent.FinishGame)
    }

    private suspend fun onItemClick(id: Int) {
        gameUseCase.onEvent(FlowGameEvent.OnItemClick(id))
//        val pointsAdded = gameUseCase.onItemClick(id)
//        _uiCallback.send(FlowGameUiCallback.PointsAdded(id, pointsAdded))
    }

    private fun onFirstVisibleItemIndexChanged(index: Int) {
//        gameUseCase.setFirstVisibleItemIndex(index)
        gameUseCase.onEvent(FlowGameEvent.FirstVisibleItemIndexChanged(index))
    }

//    private fun setItemHeight(height: Int) = gameUseCase.setItemHeight(height)
    private fun setItemHeight(height: Int) = gameUseCase.onEvent(FlowGameEvent.ItemHeightMeasured(height))

    override fun onCleared() {
        super.onCleared()
        musicManager.release()
    }
}

data class FlowGameUiState(
    val maxLifeCount: Int = 5,
    val lifeCount: Int = 0,
    val goals: Set<Goal<Any>> = emptySet(),
    val figures: List<Figure> = emptyList(),
    val score: Int = 0,
    val coefficient: Float = 0f,
    val gameDuration: Long = 0,
    val scrollDuration: Int = 0,
    val pixelsToScroll: Float = 0f,
    val rowWidth: Int = 4,
    val isActive: Boolean = true,
    val isLoading: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
)
