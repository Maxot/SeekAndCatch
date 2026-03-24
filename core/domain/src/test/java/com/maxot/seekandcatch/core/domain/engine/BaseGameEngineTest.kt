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
import kotlinx.coroutines.flow.update
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(Parameterized::class)
class BaseGameEngineTest(
    private val scoringParams: ScoringParams
) {

    data class ScoringParams(
        val difficulty: String,
        val basePoints: Int,
        val coefficient: Float,
        val expectedPoints: Int
    )

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = listOf(
            arrayOf(ScoringParams("Default", 10, 1.0f, 10)),
            arrayOf(ScoringParams("Easy-x1.5", 10, 1.5f, 15)),
            arrayOf(ScoringParams("Normal-x1", 15, 1.0f, 15)),
            arrayOf(ScoringParams("Normal-x2.3", 15, 2.3f, 34)), // 15 * 2.3 = 34.5 -> 34
            arrayOf(ScoringParams("Hard-x5.0", 20, 5.0f, 100))
        )
    }

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

    @Test
    fun scoringLogic_parameterized() {
        val customParams = gameParams.copy(scorePoint = scoringParams.basePoints)
        engine.initGame(customParams)
        testScope.testScheduler.advanceUntilIdle()
        
        // Manually set coefficient by clicking (starting from 1.0, step is 0.5)
        // Or just trigger a custom method to set it if we want exact values from params
        // For simplicity, let's add a setter to TestGameEngine
        engine.setCoefficient(scoringParams.coefficient)
        
        engine.startGame()
        engine.onItemClick(0) // Correct tap
        
        assertEquals(scoringParams.expectedPoints, engine.gameData.value.score)
    }

    @Before
    fun setup() {
        figuresRepository.setRandomFigures(testFigures)
        goalsRepository.setRandomGoal(testGoal)
        engine = TestGameEngine(testScope, figuresRepository, goalsRepository)
        engine.initGame(gameParams)
        testScope.testScheduler.advanceUntilIdle()
        
        // Ensure goals are set for the engine (sometimes FakeGoalsRepository might need it)
        engine.setGoals(setOf(testGoal))
    }

    class TestGameEngine(
        scope: TestScope,
        figuresRepo: FakeFiguresRepository,
        goalsRepo: FakeGoalsRepository
    ) : BaseGameEngine(scope, figuresRepo, goalsRepo) {
        override fun setFirstVisibleItemIndex(index: Int) {}
        override fun setItemHeight(height: Int) {}
        
        fun triggerDecreaseLife() = decreaseLifeCount()
        fun triggerDecreaseCoef() = decreaseCoefficient()
        fun setCoefficient(value: Float) {
            _gameData.update { it.copy(coefficient = value) }
        }
        fun setGoals(goals: Set<Goal<Any>>) {
            _gameData.update { it.copy(goals = goals) }
        }
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
    fun multipleCorrectTaps_increasesScoreAndCoefficientConsistently() {
        if (scoringParams.difficulty != "Default") return
        engine.startGame()
        engine.onItemClick(0) // coef 1.5, score 10
        engine.onItemClick(1) // coef 2.0, score 10 + 10*1.5 = 25
        engine.onItemClick(2) // coef 2.5, score 25 + 10*2.0 = 45
        
        val data = engine.gameData.value
        assertEquals(45, data.score)
        assertEquals(2.5f, data.coefficient, 0.01f)
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
        if (scoringParams.difficulty != "Default") return
        engine.triggerDecreaseLife() // 3 -> 2
        engine.triggerDecreaseLife() // 2 -> 1
        engine.triggerDecreaseLife() // 1 -> 0, finishes
        
        assertTrue(engine.gameState.value is GameEngineState.Finished)
    }

    @Test
    fun decreaseLifeCount_exactlyAtZero_finishesGame() {
        if (scoringParams.difficulty != "Default") return
        val customParams = gameParams.copy(lifeCount = 1)
        engine.initGame(customParams)
        engine.setGoals(setOf(testGoal))
        testScope.testScheduler.advanceUntilIdle()
        
        engine.triggerDecreaseLife() // 1 -> 0
        assertEquals(0, engine.gameData.value.lifeCount)
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
    fun lifeRecovery_exactlyAtThreshold() {
        if (scoringParams.difficulty != "Default") return
        val customParams = gameParams.copy(lifeCount = 2, maxLifeCount = 5, itemsPassedWithoutMissToGetLife = 3)
        engine.initGame(customParams)
        engine.setGoals(setOf(testGoal))
        testScope.testScheduler.advanceUntilIdle()
        engine.startGame()
        
        engine.onItemClick(0)
        engine.onItemClick(1)
        assertEquals(2, engine.gameData.value.lifeCount)
        
        engine.onItemClick(2) // 3rd correct tap
        assertEquals(3, engine.gameData.value.lifeCount)
    }

    @Test
    fun multipleCorrectTaps_inSingleFrame_consistentScore() {
        if (scoringParams.difficulty != "Default") return
        engine.startGame()
        // Simulate two taps "simultaneously" (one after another in test)
        engine.onItemClick(0) // coef 1.0 -> 1.5, score 0 -> 10
        engine.onItemClick(1) // coef 1.5 -> 2.0, score 10 -> 10 + (10 * 1.5) = 25
        
        assertEquals(2.0f, engine.gameData.value.coefficient, 0.01f)
        assertEquals(25, engine.gameData.value.score)
    }

    @Test
    fun lifeRecovery_afterEnoughCorrectTaps() {
        if (scoringParams.difficulty != "Default") return
        engine.startGame()
        engine.triggerDecreaseLife()
        assertEquals(2, engine.gameData.value.lifeCount)
        
        engine.onItemClick(0)
        engine.onItemClick(1) // itemsPassedWithoutMissToGetLife = 2
        
        assertEquals(3, engine.gameData.value.lifeCount)
    }

    @Test
    fun lifeRecovery_clampedAtMaxLife() {
        if (scoringParams.difficulty != "Default") return
        val customParams = gameParams.copy(lifeCount = 5, maxLifeCount = 5, itemsPassedWithoutMissToGetLife = 1)
        engine.initGame(customParams)
        engine.setGoals(setOf(testGoal))
        testScope.testScheduler.advanceUntilIdle()
        engine.startGame()
        
        engine.onItemClick(0)
        assertEquals(5, engine.gameData.value.lifeCount)
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
