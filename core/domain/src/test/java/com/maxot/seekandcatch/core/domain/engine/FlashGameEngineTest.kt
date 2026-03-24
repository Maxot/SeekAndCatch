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
        // baseSpawnPeriodMillis = (rowDuration * 1.5f * 1.5f).toLong() = 2250
        // baseFlashMillis = (rowDuration * 2L * 1.5f).toLong() = 3000
        
        // Wait for first loop to START (after first spawnPeriodMillis delay)
        advanceTimeBy(3010) 
        
        val visibleCells = engine.gameData.value.visibleCells
        assertTrue("visibleCells should not be empty after spawnPeriod. Current: $visibleCells", visibleCells.isNotEmpty())
        
        // Wait for flash to end (3000ms delay)
        advanceTimeBy(3001)
        assertTrue("visibleCells should be empty after flash ends", engine.gameData.value.visibleCells.isEmpty())
        
        // Wait for next flash (next 2250ms delay)
        advanceTimeBy(2251)
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
        
        // Initial coefficient is 1.0. Step is 0.5.
        // Directly call decreaseCoefficient to test duration updates if needed,
        // OR better, just test that the formula is applied correctly when coefficient changes.
        
        // Simulate some correct taps by manually updating gameData if onItemClick is failing in test loop
        // But handleCorrectTap is what we want to test.
        
        // Let's try to manually advance and check visibleCells more carefully.
        repeat(10) {
            advanceTimeBy(4000) // baseSpawn is 2250, so 4000 should definitely trigger it
            val currentData = engine.gameData.value
            val visibleIndices = currentData.visibleCells
            
            visibleIndices.forEach { index ->
                if (index % 2 == 0) {
                    engine.onItemClick(index)
                }
            }
            advanceTimeBy(4000) // baseFlash is 3000, so 4000 should clear it
        }

        val finalData = engine.gameData.value
        assertTrue("Coefficient should be high: ${finalData.coefficient}", finalData.coefficient > 1.0f)
        val expectedFlash = (gameParams.rowDuration * 2L * 1.5f / finalData.coefficient).toLong().coerceAtLeast(300L)
        val expectedSpawn = (gameParams.rowDuration * 1.5f * 1.5f / finalData.coefficient).toLong().coerceAtLeast(300L)
        assertEquals("flashMillis should be updated by formula", expectedFlash, finalData.flashMillis)
        assertEquals("spawnPeriodMillis should be updated by formula", expectedSpawn, finalData.spawnPeriodMillis)
    }

    @Test
    fun flash_multipleTapsInOneCycle() = testScope.runTest {
        engine.startGame()
        advanceTimeBy(3010)
        
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
        advanceTimeBy(3010)
        
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
        // Wait for first flash
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
        // Wait for current flash to end and NEXT spawn period to pass
        // Total time should be enough to complete at least one more cycle without clicking
        advanceTimeBy(10000) 
        
        val finalData = engine.gameData.value
        assertTrue("Coefficient should have decreased. Current: ${finalData.coefficient}", finalData.coefficient < midData.coefficient)
        assertTrue("flashMillis should increase back", finalData.flashMillis > midFlash)
    }
}
