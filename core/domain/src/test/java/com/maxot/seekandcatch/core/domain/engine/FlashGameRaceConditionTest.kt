package com.maxot.seekandcatch.core.domain.engine

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.core.common.model.GameParams
import com.maxot.seekandcatch.core.common.model.Figure
import com.maxot.seekandcatch.core.common.model.Goal
import com.maxot.seekandcatch.data.test.repository.FakeFiguresRepository
import com.maxot.seekandcatch.data.test.repository.FakeGoalsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlashGameRaceConditionTest {

    private lateinit var engine: FlashGameEngine
    private val figuresRepository = FakeFiguresRepository()
    private val goalsRepository = FakeGoalsRepository()
    private val testScope = TestScope()

    private val testGoal = Goal.Shaped(Figure.FigureType.CIRCLE)
    private val testFigures = (0 until 16).map { id ->
        Figure(
            id = id,
            type = Figure.FigureType.CIRCLE, // All are suitable
            color = Color.Red
        )
    }

    private val gameParams = GameParams(
        itemsCount = 16,
        percentOfSuitableItem = 1.0f,
        coefficientStep = 0.5f,
        scorePoint = 10,
        maxLifeCount = 5,
        lifeCount = 5,
        rowWidth = 4,
        rowDuration = 1000
    )

    @Before
    fun setup() {
        figuresRepository.setRandomFigures(testFigures)
        goalsRepository.setRandomGoal(testGoal)
        engine = FlashGameEngine(testScope, figuresRepository, goalsRepository)
        engine.initGame(gameParams)
        testScope.testScheduler.advanceUntilIdle()
    }

    @Test
    fun itemClicked_shouldNotBeMarkedAsMissed() = testScope.runTest {
        engine.startGame()
        
        // Wait for first spawn
        // baseSpawnPeriodMillis = 2250
        advanceTimeBy(2251)
        
        val currentData = engine.gameData.value
        val visibleIndices = currentData.visibleCells
        assertTrue("Should have visible cells", visibleIndices.isNotEmpty())
        
        // Click ONLY ONE visible cell
        val indexToClick = visibleIndices.first()
        engine.onItemClick(indexToClick)
        
        val coefficientAfterClick = engine.gameData.value.coefficient
        assertTrue("Coefficient should have increased", coefficientAfterClick > 1.0f)
        
        // Wait for flash duration to end
        // baseFlashMillis = 3000
        advanceTimeBy(3001)
        
        // Check coefficient again. It should have decreased because other items were missed.
        assertTrue("Coefficient should have decreased after flash ends if some items were missed", 
            engine.gameData.value.coefficient < coefficientAfterClick)
    }

    @Test
    fun decreaseLifeCount_shouldTriggerIsLifeWasted() = testScope.runTest {
        engine.startGame()
        
        // Wait for first spawn
        advanceTimeBy(2251)
        
        // Do NOT click anything. Wait for flash to end.
        // baseFlashMillis = 3000
        advanceTimeBy(3001)
        
        // At 1.0 coefficient, a miss should decrease life count.
        // It might be more than 1 miss if multiple items are visible and none are clicked.
        assertTrue("Life count should have decreased", engine.gameData.value.lifeCount < 5)
        assertFalse("isLifeWasted should be false before fix", engine.gameData.value.isLifeWasted)
    }
}
