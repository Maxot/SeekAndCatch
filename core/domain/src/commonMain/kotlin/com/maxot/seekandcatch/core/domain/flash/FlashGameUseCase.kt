package com.maxot.seekandcatch.core.domain.flash

import com.maxot.seekandcatch.core.common.model.GameParams
import com.maxot.seekandcatch.core.domain.engine.FlashGameEngine
import com.maxot.seekandcatch.core.domain.engine.GameEngineData
import com.maxot.seekandcatch.core.domain.engine.GameEngineState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class FlashGameUseCase(
    private val flashGameEngine: FlashGameEngine,
) {
    val gameState: StateFlow<FlashGameState> = combine(
        flashGameEngine.gameState,
        flashGameEngine.gameData
    ) { state, data ->
        mapToFlashGameState(state, data)
    }.stateIn(
        scope = flashGameEngine.coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FlashGameState.Idle
    )

    fun initGame(gameParams: GameParams) {
        flashGameEngine.initGame(gameParams)
    }

    fun onEvent(event: FlashGameEvent) {
        when (event) {
            FlashGameEvent.ResetGame -> flashGameEngine.reset()
            is FlashGameEvent.OnCellClick -> flashGameEngine.onItemClick(event.cellId)
            FlashGameEvent.FinishGame -> flashGameEngine.finishGame()
            FlashGameEvent.PauseGame -> flashGameEngine.pauseGame()
            FlashGameEvent.ResumeGame -> flashGameEngine.resumeGame()
            FlashGameEvent.StartGame -> flashGameEngine.startGame()
            is FlashGameEvent.Tick -> {}
        }
    }

    private fun mapToFlashGameState(state: GameEngineState, data: GameEngineData): FlashGameState {
        return when (state) {
            GameEngineState.Idle -> FlashGameState.Idle
            is GameEngineState.Created -> FlashGameState.Created(state.goalSuitableFigures)
            GameEngineState.Started -> FlashGameState.Resumed(mapToFlashGameData(data))
            GameEngineState.Paused -> FlashGameState.Paused
            is GameEngineState.Finished -> FlashGameState.Finished(state.score, mapToFlashGameData(data))
        }
    }

    private fun mapToFlashGameData(data: GameEngineData): FlashGameData {
        return FlashGameData(
            goals = data.goals,
            gridSize = data.figures.size,
            gridWidth = data.rowWidth,
            visibleCells = data.visibleCells,
            figuresByCell = data.figures.withIndex().associate { it.index to it.value },
            goalSuitableFigures = data.goalSuitableFigures,
            maxLifeCount = data.maxLifeCount,
            lifeCount = data.lifeCount,
            score = data.score,
            coefficient = data.coefficient,
            gameDuration = data.gameDuration,
            flashMillis = data.flashMillis,
            spawnPeriodMillis = data.spawnPeriodMillis,
            isLifeWasted = data.isLifeWasted,
            clickedSuitableCells = data.clickedSuitableCells
        )
    }

    fun startGame() = flashGameEngine.startGame()
    fun pauseGame() = flashGameEngine.pauseGame()
    fun resumeGame() = flashGameEngine.resumeGame()
    fun finishGame() = flashGameEngine.finishGame()
}
