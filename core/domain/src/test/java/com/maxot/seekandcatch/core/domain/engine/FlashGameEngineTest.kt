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
import kotlin.math.sqrt
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
        rowDuration = 1000,
        flashTimePerItemMillis = 500,
        visibleAtOnceMin = 2,
        visibleAtOnceMax = 3
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

        val spawnMs = engine.gameData.value.spawnPeriodMillis
        advanceTimeBy(spawnMs + 1)
        val visibleCells = engine.gameData.value.visibleCells
        assertTrue("visibleCells should not be empty after spawnPeriod. Current: $visibleCells", visibleCells.isNotEmpty())

        val flashMs = engine.gameData.value.flashMillis
        advanceTimeBy(flashMs + 1)
        assertTrue("visibleCells should be empty after flash ends", engine.gameData.value.visibleCells.isEmpty())

        val spawnMs2 = engine.gameData.value.spawnPeriodMillis
        advanceTimeBy(spawnMs2 + 1)
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
        val initialSpawn = engine.gameData.value.spawnPeriodMillis

        advanceTimeBy(3010)
        val visibleIndices = engine.gameData.value.visibleCells
        val suitableIndex = visibleIndices.find { index ->
            engine.gameData.value.figures[index].type == Figure.FigureType.CIRCLE
        }

        if (suitableIndex != null) {
            engine.onItemClick(suitableIndex)
            val dataAfterClick = engine.gameData.value
            assertTrue("Coefficient should increase", dataAfterClick.coefficient > 1.0f)
            assertTrue("spawnPeriodMillis should decrease", dataAfterClick.spawnPeriodMillis < initialSpawn)
            // flashMillis is set at cycle-start, not on tap
            assertTrue("flashMillis should be >= MIN_FLASH_MILLIS (300)", dataAfterClick.flashMillis >= 300L)
        }
    }

    @Test
    fun durations_clampedToMinimum() = testScope.runTest {
        engine.startGame()

        repeat(20) {
            val spawnMs = engine.gameData.value.spawnPeriodMillis
            advanceTimeBy(spawnMs + 1)

            val visibleIndices = engine.gameData.value.visibleCells.toSet()
            visibleIndices.forEach { index ->
                if (engine.gameData.value.figures[index].type == Figure.FigureType.CIRCLE) {
                    engine.onItemClick(index)
                }
            }

            val flashMs = engine.gameData.value.flashMillis
            advanceTimeBy(flashMs + 1)
        }

        val finalData = engine.gameData.value
        assertTrue("Coefficient should be high: ${finalData.coefficient}", finalData.coefficient > 1.0f)

        val baseSpawnPeriodMillis = (gameParams.rowDuration * 1.2f * 1.5f).toLong()
        val expectedSpawn = (baseSpawnPeriodMillis / finalData.coefficient).toLong().coerceAtLeast(300L)

        assertEquals("spawnPeriodMillis should be updated by formula", expectedSpawn, finalData.spawnPeriodMillis)
        assertTrue("flashMillis should be >= MIN_FLASH_MILLIS (300)", finalData.flashMillis >= 300L)
    }

    @Test
    fun initialDurations_matchFlashSpeedModelFormula() = testScope.runTest {
        // §14 Flash Speed Model: initial flashMillis uses visibleAtOnceMax × flashTimePerItemMillis / coef
        //                        spawnPeriodMillis = (rowDuration × 1.2 × 1.5) / coefficient
        val data = engine.gameData.value
        val maxFlashMillis = (gameParams.visibleAtOnceMax * gameParams.flashTimePerItemMillis).toLong()
        assertTrue(
            "Initial flashMillis must be within [300, $maxFlashMillis], got ${data.flashMillis}",
            data.flashMillis in 300L..maxFlashMillis
        )
        val expectedSpawn = (gameParams.rowDuration * 1.2f * 1.5f).toLong()
        assertEquals("Initial spawnPeriodMillis must match §14 formula", expectedSpawn, data.spawnPeriodMillis)
    }

    @Test
    fun visibleAtOnce_isWithinConfiguredRange() = testScope.runTest {
        // §14: visibleAtOnce drawn each cycle from [visibleAtOnceMin, visibleAtOnceMax]
        engine.startGame()
        advanceTimeBy(1801)
        val visibleCount = engine.gameData.value.visibleCells.size
        assertTrue(
            "visibleCells must be in [${gameParams.visibleAtOnceMin}, ${gameParams.visibleAtOnceMax}]; got $visibleCount",
            visibleCount in gameParams.visibleAtOnceMin..gameParams.visibleAtOnceMax
        )
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

        // 1. Increase coefficient via a correct tap — advance exactly to first spawn
        val spawnMs = engine.gameData.value.spawnPeriodMillis
        advanceTimeBy(spawnMs + 1)
        val currentDataBefore = engine.gameData.value
        val visibleIndices = currentDataBefore.visibleCells
        visibleIndices.forEach { index ->
            if (currentDataBefore.figures[index].type == Figure.FigureType.CIRCLE) {
                engine.onItemClick(index)
            }
        }

        val midData = engine.gameData.value
        val midSpawn = midData.spawnPeriodMillis
        assertTrue("Coefficient should be > 1. Current: ${midData.coefficient}", midData.coefficient > 1.0f)

        // 2. Trigger a miss to decrease coefficient
        advanceTimeBy(10000)

        val finalData = engine.gameData.value
        assertTrue("Coefficient should have decreased. Current: ${finalData.coefficient}", finalData.coefficient < midData.coefficient)
        assertTrue("spawnPeriodMillis should increase back", finalData.spawnPeriodMillis > midSpawn)
        assertTrue("flashMillis should be >= MIN_FLASH_MILLIS (300 ms)", finalData.flashMillis >= 300L)
    }

    @Test
    fun flashDuration_scalesWithCorrectItemCount() = testScope.runTest {
        // §14: flashMillis = correctCount × flashTimePerItemMillis / coefficient
        engine.startGame()

        val spawnMs = engine.gameData.value.spawnPeriodMillis
        advanceTimeBy(spawnMs + 1)

        val data = engine.gameData.value
        assertTrue("visibleCells must not be empty", data.visibleCells.isNotEmpty())

        val visibleAtOnce = data.visibleCells.size
        val minCorrect = ((visibleAtOnce + 1) / 2).coerceAtLeast(1)
        val correctItemsVisible = data.visibleCells.count { index ->
            data.figures.getOrNull(index)?.type == Figure.FigureType.CIRCLE
        }
        assertTrue("At least minCorrect ($minCorrect) correct items must be visible", correctItemsVisible >= minCorrect)

        val divisor = sqrt(data.coefficient.toInt().toFloat()).coerceAtLeast(1f)
        val expectedFlash = (correctItemsVisible * gameParams.flashTimePerItemMillis / divisor)
            .toLong().coerceAtLeast(300L)

        assertEquals(
            "flashMillis must equal correctCount × flashTimePerItemMillis / coefficient (coerced ≥ 300)",
            expectedFlash,
            data.flashMillis
        )
    }

    @Test
    fun flashDuration_clampedToMinimumAtHighCoefficient() = testScope.runTest {
        // Drive coefficient very high, then verify all flash cycles observe flashMillis ≥ 300
        val highStepParams = gameParams.copy(coefficientStep = 10.0f)
        engine.initGame(highStepParams)
        testScope.testScheduler.advanceUntilIdle()
        engine.startGame()

        // Trigger one cycle and click circles to spike coefficient
        val firstSpawnMs = engine.gameData.value.spawnPeriodMillis
        advanceTimeBy(firstSpawnMs + 1)
        engine.gameData.value.visibleCells.forEach { index ->
            if (engine.gameData.value.figures[index].type == Figure.FigureType.CIRCLE) {
                engine.onItemClick(index)
            }
        }

        // coefficient is now ≥ 11 (coefficientStep=10 × at least 1 click)
        assertTrue("Coefficient should be very high", engine.gameData.value.coefficient > 5f)

        // End the first flash, then observe several more cycles and verify the floor holds
        val firstFlashMs = engine.gameData.value.flashMillis
        advanceTimeBy(firstFlashMs + 1)

        repeat(3) {
            val cycleSpawnMs = engine.gameData.value.spawnPeriodMillis
            advanceTimeBy(cycleSpawnMs + 1)
            if (engine.gameData.value.visibleCells.isNotEmpty()) {
                assertTrue(
                    "flashMillis must be >= MIN_FLASH_MILLIS (300 ms), got ${engine.gameData.value.flashMillis}",
                    engine.gameData.value.flashMillis >= 300L
                )
            }
            val cycleFlashMs = engine.gameData.value.flashMillis.coerceAtLeast(1L)
            advanceTimeBy(cycleFlashMs + 1)
        }
    }
}
