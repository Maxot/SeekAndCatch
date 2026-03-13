package com.maxot.seekandcatch.core.domain.engine

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.core.common.model.GameParams
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.test.repository.FakeFiguresRepository
import com.maxot.seekandcatch.data.test.repository.FakeGoalsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseGameEngineTest {

    private lateinit var engine: TestGameEngine
    private val figuresRepository = FakeFiguresRepository()
    private val goalsRepository = FakeGoalsRepository()
    private val testScope = TestScope()

    private val testGoal = Goal.Shaped(Figure.FigureType.CIRCLE)
    private val testFigures = listOf(
        Figure(id = 0, type = Figure.FigureType.CIRCLE, color = Color.Red),
        Figure(id = 1, type = Figure.FigureType.CIRCLE, color = Color.Blue),
        Figure(id = 2, type = Figure.FigureType.CIRCLE, color = Color.Green),
        Figure(id = 3, type = Figure.FigureType.TRIANGLE, color = Color.Blue),
        Figure(id = 4, type = Figure.FigureType.SQUARE, color = Color.Yellow)
    )

    private val gameParams = GameParams(
        itemsCount = 5,
        percentOfSuitableItem = 0.6f,
        coefficientStep = 0.5f,
        scorePoint = 10,
        maxLifeCount = 5,
        lifeCount = 3,
        itemsPassedWithoutMissToGetLife = 2
    )

    class TestGameEngine(
        scope: TestScope,
        figuresRepo: FakeFiguresRepository,
        goalsRepo: FakeGoalsRepository
    ) : BaseGameEngine(scope, figuresRepo, goalsRepo) {
        override fun setFirstVisibleItemIndex(index: Int) {}
        override fun setItemHeight(height: Int) {}
        
        fun triggerDecreaseLife() = decreaseLifeCount()
        fun triggerDecreaseCoef() = decreaseCoefficient()
    }

    @Before
    fun setup() {
        figuresRepository.setRandomFigures(testFigures)
        goalsRepository.setRandomGoal(testGoal)
        engine = TestGameEngine(testScope, figuresRepository, goalsRepository)
        engine.initGame(gameParams)
        testScope.testScheduler.advanceUntilIdle()
    }

    @Test
    fun initGame_initialStateCorrect() {
        val data = engine.gameData.value
        assertEquals(3, data.lifeCount)
        assertEquals(1f, data.coefficient)
        assertEquals(0, data.score)
        assertTrue(engine.gameState.value is GameEngineState.Created)
    }

    @Test
    fun startGame_stateIsStarted() {
        engine.startGame()
        assertTrue(engine.gameState.value is GameEngineState.Started)
    }

    @Test
    fun pauseResume_stateTransitionsCorrectly() {
        engine.startGame()
        engine.pauseGame()
        assertTrue(engine.gameState.value is GameEngineState.Paused)
        engine.resumeGame()
        assertTrue(engine.gameState.value is GameEngineState.Started)
    }

    @Test
    fun correctTap_increasesScoreAndCoefficientOnEveryTap() {
        engine.startGame()
        engine.onItemClick(0) // 0 is CIRCLE, which matches testGoal
        
        val data1 = engine.gameData.value
        assertEquals(10, data1.score)
        assertEquals(1.5f, data1.coefficient, 0.01f) // Increased by 0.5 on first tap

        engine.onItemClick(1)
        val data2 = engine.gameData.value
        assertEquals(25, data2.score) // 10 + (10 * 1.5) = 25
        assertEquals(2.0f, data2.coefficient, 0.01f) // Increased by another 0.5
    }

    @Test
    fun wrongTap_finishesGame() {
        engine.startGame()
        engine.onItemClick(3) // 3 is TRIANGLE, which doesn't match testGoal
        
        assertTrue(engine.gameState.value is GameEngineState.Finished)
    }

    @Test
    fun decreaseLifeCount_works() {
        engine.triggerDecreaseLife()
        assertEquals(2, engine.gameData.value.lifeCount)
    }

    @Test
    fun lifeReachesZero_finishesGame() {
        engine.triggerDecreaseLife() // 3 -> 2
        engine.triggerDecreaseLife() // 2 -> 1
        engine.triggerDecreaseLife() // 1 -> 0, finishes
        
        assertTrue(engine.gameState.value is GameEngineState.Finished)
    }

    @Test
    fun decreaseCoefficient_halvesAndClampsAtOne() {
        engine.startGame()
        engine.onItemClick(0) // coef becomes 1.5f
        assertEquals(1.5f, engine.gameData.value.coefficient, 0.01f)
        
        engine.triggerDecreaseCoef() // halves 1.5f to 0.75f, clamped at 1.0f
        assertEquals(1f, engine.gameData.value.coefficient, 0.01f)
    }

    @Test
    fun lifeRecovery_afterEnoughCorrectTaps() {
        engine.startGame()
        engine.triggerDecreaseLife()
        assertEquals(2, engine.gameData.value.lifeCount)
        
        engine.onItemClick(0)
        engine.onItemClick(1) // itemsPassedWithoutMissToGetLife = 2
        
        assertEquals(3, engine.gameData.value.lifeCount)
    }

    @Test
    fun timeTracking_updatesDuration() = runTest {
        engine.startGame()
        testScope.testScheduler.advanceTimeBy(2001)
        testScope.testScheduler.runCurrent()
        assertTrue("Game duration should be at least 2000, but was ${engine.gameData.value.gameDuration}", 
            engine.gameData.value.gameDuration >= 2000)
    }
}
