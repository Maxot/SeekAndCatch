package com.maxot.seekandcatch.core.domain

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.GameParams
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.test.repository.FakeFiguresRepository
import com.maxot.seekandcatch.data.test.repository.FakeGoalsRepository
import com.maxot.seekandcatch.data.test.repository.FakeScoreRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FlowGameUseCaseTest {

    private val figuresRepository = FakeFiguresRepository()
    private val goalsRepository = FakeGoalsRepository()
    private val scoreRepository = FakeScoreRepository()

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
        figuresRepository = figuresRepository,
        scoreRepository = scoreRepository,
        goalsRepository = goalsRepository
    )
    private val gameParam = GameParams(
        itemsCount = 10,
        percentOfSuitableItem = 0.5f,
        coefficientStep = 0.25f,
        scorePoint = 10
    )

    @Before
    fun setup() {
        figuresRepository.setRandomFigures(testRandomFigures)
        goalsRepository.setRandomGoal(testGoal)
        useCase.initGame(gameParam)
    }

    @Test
    fun startGame_gameStateIsStarted() = runTest {
        useCase.startGame()

        assertEquals(getCurrentGameState(), GameState.STARTED)
    }

    @Test
    fun finishGame_gameStateIsFinished() = runTest {
        useCase.finishGame()
        val currentGameState = useCase.gameState.value
        assertEquals(currentGameState, GameState.FINISHED)
    }

    @Test
    fun onClick_notFitForGoal_gameFinished() {
        useCase.onItemClick(id = 5)

        assertEquals(getCurrentGameState(), GameState.FINISHED)
    }

    @Test
    fun onItemClick_fitForGoal_figureIsActiveFalse() {
        val beforeClickCoef = useCase.coefficient.value
        val beforeClickScore = useCase.score.value

        useCase.onItemClick(id = 0)

        val clickedFigure = testRandomFigures.find { it.id == 0 }

        val afterClickCoef = useCase.coefficient.value
        val afterClickScore = useCase.score.value

        assertEquals(clickedFigure!!.isActive, false)
        assertEquals(beforeClickCoef + gameParam.coefficientStep, afterClickCoef)
        assertEquals(beforeClickScore + gameParam.scorePoint, afterClickScore)
    }

    @Test
    fun onItemsMissed_coefficientDecrease() {
        // Need for increase coefficient
        useCase.onItemClick(id = 0)
        useCase.onItemClick(id = 6)
        useCase.onItemClick(id = 9)  // Now coefficient must be 1.75f

        assertEquals(getCurrentCoefficient(), 1.75f)

        useCase.onItemsMissed(0, 4) // One item missed, id = 3

        assertEquals(getCurrentCoefficient(), 1.5f)
    }

    private fun getCurrentGameState() = useCase.gameState.value
    private fun getCurrentCoefficient() = useCase.coefficient.value


}
