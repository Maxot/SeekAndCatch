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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlashGameEngineTest {

    private lateinit var engine: FlashGameEngine
    private val figuresRepository = FakeFiguresRepository()
    private val goalsRepository = FakeGoalsRepository()
    private val testScope = TestScope()

    private val testGoal = Goal.Shaped(Figure.FigureType.CIRCLE)
    private val testFigures = (0 until 16).map { id ->
        Figure(
            id = id,
            type = if (id % 2 == 0) Figure.FigureType.CIRCLE else Figure.FigureType.TRIANGLE,
            color = Color.Red
        )
    }

    private val gameParams = GameParams(
        itemsCount = 16,
        percentOfSuitableItem = 0.5f,
        coefficientStep = 0.5f,
        scorePoint = 10,
        maxLifeCount = 10,
        lifeCount = 10,
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
    fun flashLoop_updatesVisibleCells() = testScope.runTest {
        engine.startGame()
        
        // At start, visibleCells should be empty
        // FlashGameEngine calculates:
        // spawnPeriodMillis = (rowDuration * 1.5f).toLong() = 1500
        // flashMillis = (rowDuration * 2L).toLong() = 2000
        
        // Wait for first loop to START (after first spawnPeriodMillis delay)
        advanceTimeBy(1501) 
        
        val visibleCells = engine.gameData.value.visibleCells
        assertTrue("visibleCells should not be empty after spawnPeriod. Current: $visibleCells", visibleCells.isNotEmpty())
        
        // Wait for flash to end (2000ms delay)
        advanceTimeBy(2001)
        assertTrue("visibleCells should be empty after flash ends", engine.gameData.value.visibleCells.isEmpty())
        
        // Wait for next flash (next 1500ms delay)
        advanceTimeBy(1501)
        assertTrue("visibleCells should not be empty for second flash. Current: ${engine.gameData.value.visibleCells}", engine.gameData.value.visibleCells.isNotEmpty())
    }

    @Test
    fun onItemClick_correct_updatesScore() = testScope.runTest {
        engine.startGame()
        advanceTimeBy(1501)
        
        val visibleIndices = engine.gameData.value.visibleCells
        val suitableIndex = visibleIndices.find { index ->
            engine.gameData.value.figures[index].type == Figure.FigureType.CIRCLE
        }
        
        if (suitableIndex != null) {
            val initialScore = engine.gameData.value.score
            engine.onItemClick(suitableIndex)
            assertTrue("Score should increase after correct tap", engine.gameData.value.score > initialScore)
        }
    }

    @Test
    fun coefficientIncrease_decreasesDurations() = testScope.runTest {
        engine.startGame()
        val initialFlash = engine.gameData.value.flashMillis
        val initialSpawn = engine.gameData.value.spawnPeriodMillis

        // Find a suitable index and click it multiple times to increase coefficient
        // In this test setup, coefficientStep is 0.5f
        advanceTimeBy(1501)
        val visibleIndices = engine.gameData.value.visibleCells
        val suitableIndex = visibleIndices.find { index ->
            engine.gameData.value.figures[index].type == Figure.FigureType.CIRCLE
        }
        
        if (suitableIndex != null) {
            engine.onItemClick(suitableIndex)
            val dataAfterClick = engine.gameData.value
            assertTrue("Coefficient should increase", dataAfterClick.coefficient > 1.0f)
            assertTrue("flashMillis should decrease", dataAfterClick.flashMillis < initialFlash)
            assertTrue("spawnPeriodMillis should decrease", dataAfterClick.spawnPeriodMillis < initialSpawn)
        }
    }

    @Test
    fun durations_clampedToMinimum() = testScope.runTest {
        engine.startGame()
        
        // Increase coefficient by 10 steps manually
        // Initial coef is 1.0. Step is 0.5. 10 steps -> 6.0
        repeat(10) {
            val currentSpawn = engine.gameData.value.spawnPeriodMillis
            // We need to wait for the spawn to happen in the background loop
            advanceTimeBy(currentSpawn + 10)
            
            val currentDataAtIteration = engine.gameData.value
            val visibleIndices = currentDataAtIteration.visibleCells
            
            // In the test setup, even indices are CIRCLE (suitable)
            // Figure(id = id, type = if (id % 2 == 0) CIRCLE else TRIANGLE)
            visibleIndices.forEach { index ->
                if (index % 2 == 0) {
                    engine.onItemClick(index)
                }
            }
            
            val currentFlash = engine.gameData.value.flashMillis
            advanceTimeBy(currentFlash + 10)
        }

        val finalData = engine.gameData.value
        assertTrue("Coefficient should be high: ${finalData.coefficient}", finalData.coefficient > 3.0f)
        val expectedFlash = (gameParams.rowDuration * 2L * 0.35f).toLong()
        val expectedSpawn = (gameParams.rowDuration * 1.5f * 0.35f).toLong()
        assertEquals("flashMillis should be clamped by 0.35", expectedFlash, finalData.flashMillis)
        assertEquals("spawnPeriodMillis should be clamped by 0.35", expectedSpawn, finalData.spawnPeriodMillis)
    }

    @Test
    fun coefficientDecrease_increasesDurations() = testScope.runTest {
        engine.startGame()
        
        // 1. Increase coefficient
        // Wait for first flash
        advanceTimeBy(1501)
        val currentDataBefore = engine.gameData.value
        val visibleIndices = currentDataBefore.visibleCells
        visibleIndices.forEach { index ->
            if (currentDataBefore.figures[index].type == Figure.FigureType.CIRCLE) {
                engine.onItemClick(index)
            }
        }
        
        val midData = engine.gameData.value
        val midFlash = midData.flashMillis
        assertTrue("Coefficient should be > 1. Current: ${midData.coefficient}", midData.coefficient > 1.0f)

        // 2. Trigger a miss to decrease coefficient
        // Wait for current flash to end and NEXT spawn period to pass
        // Total time should be enough to complete at least one more cycle without clicking
        advanceTimeBy(10000) 
        
        val finalData = engine.gameData.value
        assertTrue("Coefficient should have decreased. Current: ${finalData.coefficient}", finalData.coefficient < midData.coefficient)
        assertTrue("flashMillis should increase back", finalData.flashMillis > midFlash)
    }
}
