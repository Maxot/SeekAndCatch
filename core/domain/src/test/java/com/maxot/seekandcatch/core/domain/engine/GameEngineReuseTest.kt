package com.maxot.seekandcatch.core.domain.engine

import com.maxot.seekandcatch.core.common.model.GameParams
import com.maxot.seekandcatch.core.common.model.Figure
import com.maxot.seekandcatch.core.common.model.Goal
import com.maxot.seekandcatch.data.test.repository.FakeFiguresRepository
import com.maxot.seekandcatch.data.test.repository.FakeGoalsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameEngineReuseTest {

    private lateinit var engine: FlowGameEngine
    private val figuresRepository = FakeFiguresRepository()
    private val goalsRepository = FakeGoalsRepository()
    private val testScope = TestScope()

    private val testGoal = Goal.Shaped(Figure.FigureType.CIRCLE)
    private val testFigures = List(100) { Figure(id = it, type = Figure.FigureType.CIRCLE, color = androidx.compose.ui.graphics.Color.Red) }

    private val gameParams = GameParams(
        itemsCount = 100,
        percentOfSuitableItem = 1.0f,
        coefficientStep = 0.5f,
        scorePoint = 10,
        maxLifeCount = 5,
        lifeCount = 3,
        itemsPassedWithoutMissToGetLife = 100
    )

    @Before
    fun setup() {
        figuresRepository.setRandomFigures(testFigures)
        goalsRepository.setRandomGoal(testGoal)
        engine = FlowGameEngine(testScope, figuresRepository, goalsRepository)
    }

    @Test
    fun engineReuse_whenFinished_thenInit_shouldNotBeFinished() = runTest {
        // 1. Start and Finish first game
        engine.initGame(gameParams)
        testScope.testScheduler.advanceUntilIdle()
        engine.startGame()
        engine.finishGame()
        assertTrue(engine.gameState.value is GameEngineState.Finished)

        // 2. Init second game
        engine.initGame(gameParams)
        // Immediately after initGame, state should be Idle or Created, NOT Finished
        assertFalse("State should not be Finished after initGame", engine.gameState.value is GameEngineState.Finished)
        
        testScope.testScheduler.advanceUntilIdle()
        assertTrue("State should be Created after initGame completes", engine.gameState.value is GameEngineState.Created)
    }

    @Test
    fun engineReuse_whenFinished_thenInit_shouldResetData() = runTest {
        // 1. Play first game to get some score
        engine.initGame(gameParams)
        testScope.testScheduler.advanceUntilIdle()
        engine.startGame()
        engine.onItemClick(0)
        assertTrue(engine.gameData.value.score > 0)
        engine.finishGame()

        // 2. Init second game
        engine.initGame(gameParams)
        assertEquals(0, engine.gameData.value.score)
    }
}
