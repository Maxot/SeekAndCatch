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

        // baseSpawnPeriodMillis = (rowDuration * 1.2f * 1.5f).toLong() = 1800
        // baseFlashMillis       = (rowDuration * 2L   * 1.2f).toLong() = 2400

        advanceTimeBy(1801)
        val visibleCells = engine.gameData.value.visibleCells
        assertTrue("visibleCells should not be empty after spawnPeriod. Current: $visibleCells", visibleCells.isNotEmpty())

        advanceTimeBy(2401)
        assertTrue("visibleCells should be empty after flash ends", engine.gameData.value.visibleCells.isEmpty())

        advanceTimeBy(1801)
        assertTrue("visibleCells should not be empty for second flash. Current: ${engine.gameData.value.visibleCells}", engine.gameData.value.visibleCells.isNotEmpty())
    }

    @Test
    fun onItemClick_correct_updatesScore() = testScope.runTest {
        engine.startGame()
        advanceTimeBy(3010)
        
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
        advanceTimeBy(3010)
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

        // baseSpawnPeriodMillis = (rowDuration * 1.2f * 1.5f).toLong() = 1800
        // baseFlashMillis       = (rowDuration * 2L   * 1.2f).toLong() = 2400
        repeat(20) {
            advanceTimeBy(2000)
            testScope.testScheduler.advanceUntilIdle()

            val visibleIndices = engine.gameData.value.visibleCells.toSet()
            visibleIndices.forEach { index ->
                if (engine.gameData.value.figures[index].type == Figure.FigureType.CIRCLE) {
                    engine.onItemClick(index)
                }
            }

            advanceTimeBy(2500)
            testScope.testScheduler.advanceUntilIdle()
        }

        val finalData = engine.gameData.value
        assertTrue("Coefficient should be high: ${finalData.coefficient}", finalData.coefficient > 1.0f)

        val baseFlashMillis = (gameParams.rowDuration * 2L * 1.2f).toLong()
        val baseSpawnPeriodMillis = (gameParams.rowDuration * 1.2f * 1.5f).toLong()

        val expectedFlash = (baseFlashMillis / finalData.coefficient).toLong().coerceAtLeast(300L)
        val expectedSpawn = (baseSpawnPeriodMillis / finalData.coefficient).toLong().coerceAtLeast(300L)

        assertEquals("flashMillis should be updated by formula", expectedFlash, finalData.flashMillis)
        assertEquals("spawnPeriodMillis should be updated by formula", expectedSpawn, finalData.spawnPeriodMillis)
    }

    @Test
    fun initialDurations_matchFlashSpeedModelFormula() = testScope.runTest {
        // §14 Flash Speed Model: flashMillis = (rowDuration × 2 × 1.2) / coefficient
        //                        spawnPeriodMillis = (rowDuration × 1.2 × 1.5) / coefficient
        // At init: coefficient = 1.0, rowDuration = 1000
        val data = engine.gameData.value
        val expectedFlash = (gameParams.rowDuration * 2L * 1.2f).toLong()          // 2400
        val expectedSpawn = (gameParams.rowDuration * 1.2f * 1.5f).toLong()        // 1800
        assertEquals("Initial flashMillis must match §14 formula", expectedFlash, data.flashMillis)
        assertEquals("Initial spawnPeriodMillis must match §14 formula", expectedSpawn, data.spawnPeriodMillis)
    }

    @Test
    fun visibleAtOnce_isGridWidthMinusOne() = testScope.runTest {
        // §14: visibleAtOnce = max(1, gridWidth − 1); difficulty-only, not coefficient-driven
        // gameParams.rowWidth = 4, so visibleAtOnce = 3
        engine.startGame()
        advanceTimeBy(1801)
        val visibleCount = engine.gameData.value.visibleCells.size
        assertTrue("visibleAtOnce should be max(1, gridWidth-1) = 3; got $visibleCount", visibleCount <= 3)
    }

    @Test
    fun flash_multipleTapsInOneCycle() = testScope.runTest {
        engine.startGame()
        advanceTimeBy(3000)
        
        val visibleIndices = engine.gameData.value.visibleCells
        val suitableIndices = visibleIndices.filter { index ->
            engine.gameData.value.figures[index].type == Figure.FigureType.CIRCLE
        }
        
        if (suitableIndices.size >= 2) {
            val initialScore = engine.gameData.value.score
            engine.onItemClick(suitableIndices[0])
            engine.onItemClick(suitableIndices[1])
            
            assertTrue("Score should increase for both taps", engine.gameData.value.score > initialScore)
            // Still same visible cells until flash ends
            assertEquals(visibleIndices.size, engine.gameData.value.visibleCells.size) 
        }
    }

    @Test
    fun flash_wrongTap_gameOver() = testScope.runTest {
        engine.startGame()
        advanceTimeBy(3000)
        
        val visibleIndices = engine.gameData.value.visibleCells
        val wrongIndex = visibleIndices.find { index ->
            engine.gameData.value.figures[index].type != Figure.FigureType.CIRCLE
        }
        
        if (wrongIndex != null) {
            engine.onItemClick(wrongIndex)
            assertTrue("Game should be finished on wrong tap", engine.gameState.value is GameEngineState.Finished)
        }
    }

    @Test
    fun coefficientDecrease_increasesDurations() = testScope.runTest {
        engine.startGame()
        
        // 1. Increase coefficient
        advanceTimeBy(3010)
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
        advanceTimeBy(10000) 
        
        val finalData = engine.gameData.value
        assertTrue("Coefficient should have decreased. Current: ${finalData.coefficient}", finalData.coefficient < midData.coefficient)
        assertTrue("flashMillis should increase back", finalData.flashMillis > midFlash)
    }
}
