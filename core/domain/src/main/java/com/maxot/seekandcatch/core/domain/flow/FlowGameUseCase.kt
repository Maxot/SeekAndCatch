package com.maxot.seekandcatch.core.domain.flow

import com.maxot.seekandcatch.core.common.di.ApplicationScope
import com.maxot.seekandcatch.core.common.model.GameParams
import com.maxot.seekandcatch.core.domain.engine.FlowGameEngine
import com.maxot.seekandcatch.core.domain.engine.GameEngineData
import com.maxot.seekandcatch.core.domain.engine.GameEngineState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

const val TAG = "FlowGameUseCase"


/**
 * Represent game logic for Flow Game Mode.
 * The game consists of grid of [FlowGameData.figures] with fixed [FlowGameData.rowWidth] and the [FlowGameData.goals].
 * Items are auto-scrolled with some speed(px/ms), the amount of pixel to scroll calculated by the engine
 * and the scroll duration calculated by the engine. Then UI pass back first visible item index
 * to process game updates via the engine.
 * During this process user should click on items and the engine will check if the item is fit for the goals
 * and update the score, coefficient, and life accordingly.
 */
class FlowGameUseCase
@Inject constructor(
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val flowGameEngine: FlowGameEngine
) {

    val gameState: StateFlow<FlowGameState> = combine(
        flowGameEngine.gameState,
        flowGameEngine.gameData
    ) { engineState, engineData ->
        mapToFlowGameState(engineState, engineData)
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = FlowGameState.Idle
    )

    fun initGame(gameParams: GameParams) {
        flowGameEngine.initGame(gameParams)
    }


    fun onEvent(event: FlowGameEvent) {
        when (event) {
            is FlowGameEvent.OnItemClick -> flowGameEngine.onItemClick(event.itemId)
            FlowGameEvent.UpdateScrollDuration -> { /* Handled by engine */ }
            FlowGameEvent.UpdatePixelsToScroll -> { /* Handled by engine */ }
            FlowGameEvent.FinishGame -> flowGameEngine.finishGame()
            FlowGameEvent.PauseGame -> flowGameEngine.pauseGame()
            FlowGameEvent.ResumeGame -> flowGameEngine.resumeGame()
            FlowGameEvent.StartGame -> flowGameEngine.startGame()
            is FlowGameEvent.FirstVisibleItemIndexChanged -> flowGameEngine.setFirstVisibleItemIndex(event.firstVisibleItemIndex)
            is FlowGameEvent.ItemHeightMeasured -> flowGameEngine.setItemHeight(event.height)
        }

    }

    private fun mapToFlowGameState(engineState: GameEngineState, engineData: GameEngineData): FlowGameState {
        return when (engineState) {
            GameEngineState.Idle -> FlowGameState.Idle
            is GameEngineState.Created -> FlowGameState.Created(engineState.goalSuitableFigures)
            GameEngineState.Started -> FlowGameState.Resumed(mapToFlowGameData(engineData))
            GameEngineState.Paused -> FlowGameState.Paused
            is GameEngineState.Finished -> FlowGameState.Finished(engineState.score)
        }
    }

    private fun mapToFlowGameData(engineData: GameEngineData): FlowGameData {
        return FlowGameData(
            goals = engineData.goals,
            figures = engineData.figures,
            goalSuitableFigures = engineData.goalSuitableFigures,
            maxLifeCount = engineData.maxLifeCount,
            lifeCount = engineData.lifeCount,
            score = engineData.score,
            coefficient = engineData.coefficient,
            gameDuration = engineData.gameDuration,
            scrollDuration = engineData.scrollDuration,
            pixelsToScroll = engineData.pixelsToScroll,
            rowWidth = engineData.rowWidth,
            isReverseScrolling = engineData.isReverseScrolling,
            isLifeWasted = engineData.isLifeWasted
        )
    }
}
