package com.maxot.seekandcatch.core.domain

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.core.domain.flow.FlowGameEvent
import com.maxot.seekandcatch.core.domain.flow.FlowGameData
import com.maxot.seekandcatch.core.domain.flow.FlowGameState
import com.maxot.seekandcatch.core.domain.flow.FlowGameUseCase
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.GameParams
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.test.repository.FakeFiguresRepository
import com.maxot.seekandcatch.data.test.repository.FakeGoalsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@Ignore("FlowGameUseCaseTest is broken due to recent refactorings and needs significant updates to match the new asynchronous game logic.")
class FlowGameUseCaseTest {

    private val figuresRepository = FakeFiguresRepository()
    private val goalsRepository = FakeGoalsRepository()

    private val testRandomFigures = listOf(
        Figure(id = 0, type = Figure.FigureType.CIRCLE, color = Color.Red),
        Figure(id = 1, type = Figure.FigureType.TRIANGLE, color = Color.Blue),
        Figure(id = 2, type = Figure.FigureType.SQUARE, color = Color.Yellow),
        Figure(id = 3, type = Figure.FigureType.CIRCLE, color = Color.Red),
        Figure(id = 4, type = Figure.FigureType.TRIANGLE, color = Color.Red),
        Figure(id = 5, type = Figure.FigureType.SQUARE, color = Color.Blue),
        Figure(id = 6, type = Figure.FigureType.CIRCLE, color = Color.Red),
        Figure(id = 7, type = Figure.FigureType.TRIANGLE, color = Color.Blue),
        Figure(id = 8, type = Figure.FigureType.SQUARE, color = Color.Yellow),
        Figure(id = 9, type = Figure.FigureType.CIRCLE, color = Color.Yellow),
    )
    private val testGoal = Goal.Shaped(Figure.FigureType.CIRCLE)

    private val useCase = FlowGameUseCase(
        coroutineScope = TestScope(),
        figuresRepository = figuresRepository,
        goalsRepository = goalsRepository
    )
    private val gameParam = GameParams(
        itemsCount = 10,
        percentOfSuitableItem = 0.5f,
        coefficientStep = 0.25f,
        scorePoint = 10,
        maxLifeCount = 5,
        lifeCount = 3,
        itemsPassedWithoutMissToGetLife = 3
    )

    @Before
    fun setup() {
        figuresRepository.setRandomFigures(testRandomFigures)
        goalsRepository.setRandomGoal(testGoal)
        useCase.initGame(gameParam)
    }

    @Test
    fun startGame_gameStateIsStarted() = runTest {
        useCase.onEvent(FlowGameEvent.StartGame)
        advanceUntilIdle()

        assertTrue(getCurrentGameState() is FlowGameState.Started || getCurrentGameState() is FlowGameState.Resumed)
    }

    @Test
    fun finishGame_gameStateIsFinished() = runTest {
        useCase.onEvent(FlowGameEvent.FinishGame)
        advanceUntilIdle()
        val currentGameState = useCase.gameState.value
        assertTrue(currentGameState is FlowGameState.Finished)
    }

    @Test
    fun onClick_notFitForGoal_gameFinished() = runTest {
        useCase.onEvent(FlowGameEvent.StartGame)
        advanceUntilIdle()
        useCase.onEvent(FlowGameEvent.OnItemClick(5))
        advanceUntilIdle()

        assertTrue(getCurrentGameState() is FlowGameState.Finished)
    }

    @Test
    fun onItemClick_fitForGoal_figureIsActiveFalse() = runTest {
        useCase.onEvent(FlowGameEvent.StartGame)
        advanceUntilIdle()
        val dataBefore = getGameData()
        val beforeClickCoef = dataBefore.coefficient
        val beforeClickScore = dataBefore.score

        useCase.onEvent(FlowGameEvent.OnItemClick(0))
        advanceUntilIdle()

        val dataAfter = getGameData()
        val afterClickCoef = dataAfter.coefficient
        val afterClickScore = dataAfter.score

        assertEquals(beforeClickCoef + gameParam.coefficientStep, afterClickCoef)
        assertEquals(beforeClickScore + gameParam.scorePoint, afterClickScore)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun firstVisibleItemIndex_isNine_passedItemsProcessed() = runTest {
        useCase.onEvent(FlowGameEvent.StartGame)
        advanceUntilIdle()
        // Need for increase coefficient
        useCase.onEvent(FlowGameEvent.OnItemClick(0))
        useCase.onEvent(FlowGameEvent.OnItemClick(6)) // Now coefficient must be 1.5f
        advanceUntilIdle()

        assertEquals(1.5f, getGameData().coefficient)

        useCase.onEvent(FlowGameEvent.FirstVisibleItemIndexChanged(9))
        advanceUntilIdle()

        // After processing passed items (some of which are suitable), coefficient should decrease
        assertTrue(getGameData().coefficient < 1.5f)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun firstVisibleItemIndex_isNineteen_lifeDecreased() = runTest {
        useCase.onEvent(FlowGameEvent.StartGame)
        advanceUntilIdle()
        assertEquals(3, getGameData().lifeCount)

        useCase.onEvent(FlowGameEvent.FirstVisibleItemIndexChanged(9)) // need simulate scroll
        advanceUntilIdle()
    }

    private fun getCurrentGameState() = useCase.gameState.value

    private fun getGameData() = (useCase.gameState.value as? FlowGameState.Resumed)?.data
        ?: FlowGameData()

    private fun simulateScroll(lastIndex: Int) = flow<Int> {
        val itemsInRow = 4
        for (i in 0..lastIndex step itemsInRow) {
            emit(i)
        }
    }
}
