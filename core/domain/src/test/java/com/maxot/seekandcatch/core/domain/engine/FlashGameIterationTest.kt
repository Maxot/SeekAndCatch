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
class FlashGameIterationTest {

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
    fun iterations_noFalseMissesWhenItemsClickedAtEdge() = testScope.runTest {
        engine.startGame()

        // Wait for first spawn
        val spawnMs = engine.gameData.value.spawnPeriodMillis
        advanceTimeBy(spawnMs + 1)

        val visibleCells1 = engine.gameData.value.visibleCells
        assertTrue("Should have visible cells", visibleCells1.isNotEmpty())

        val flashMs1 = engine.gameData.value.flashMillis

        // Click all items 2 ms before the flash ends (1 ms buffer on each side)
        advanceTimeBy(flashMs1 - 2)
        visibleCells1.forEach { engine.onItemClick(it) }

        // Advance past the flash end
        advanceTimeBy(2)

        // No misses — all suitable items were clicked before flash ended
        assertEquals("Should have no health loss", 5, engine.gameData.value.lifeCount)
        assertEquals("Coefficient should have increased", 1.0f + visibleCells1.size * 0.5f, engine.gameData.value.coefficient, 0.01f)

        // Wait for second iteration
        val spawnDelay2 = engine.gameData.value.spawnPeriodMillis
        advanceTimeBy(spawnDelay2 + 1)
        
        val visibleCells2 = engine.gameData.value.visibleCells
        assertTrue("Should have new visible cells", visibleCells2.isNotEmpty())
        
        // Wait for it to end WITHOUT clicking
        val flashDelay2 = engine.gameData.value.flashMillis
        advanceTimeBy(flashDelay2 + 1)
        
        // At 1.0 coefficient, miss should decrease life
        assertTrue("Should have lost health. Current: ${engine.gameData.value.lifeCount}", engine.gameData.value.lifeCount < 5)
        assertTrue("isLifeWasted should be true", engine.gameData.value.isLifeWasted)
    }

    @Test
    fun iterations_clickedItemsAreNeverMissedEvenIfVisibleIndicesChange() = testScope.runTest {
        engine.startGame()
        
        // Iteration 1
        advanceTimeBy(1801)
        val visibleCells1 = engine.gameData.value.visibleCells.toSet()
        
        // Click only one
        val clicked = visibleCells1.first()
        engine.onItemClick(clicked)
        val coefAfterClick = engine.gameData.value.coefficient
        
        // Wait for it to end
        advanceTimeBy(engine.gameData.value.flashMillis + 10) // Small buffer
        testScope.testScheduler.advanceUntilIdle()
        
        val missedCount = visibleCells1.size - 1
        var expectedCoef = coefAfterClick
        repeat(missedCount) {
            expectedCoef = (expectedCoef / 2f).coerceAtLeast(1f)
        }
        
        assertEquals("Coefficient should reflect only unclicked items being missed", expectedCoef, engine.gameData.value.coefficient, 0.01f)
    }

    @Test
    fun iterations_noMissDuringSpawnPeriod() = testScope.runTest {
        engine.startGame()
        
        // Move past Iteration 1
        advanceTimeBy(1800 + 2400 + 1)
        val lifeAfter1 = engine.gameData.value.lifeCount
        val coefAfter1 = engine.gameData.value.coefficient
        
        // Now in spawnPeriod of Iteration 2. Wait some time but not enough to start Iteration 2.
        advanceTimeBy(500)
        
        assertEquals("Life should not change during spawn period", lifeAfter1, engine.gameData.value.lifeCount)
        assertEquals("Coefficient should not change during spawn period", coefAfter1, engine.gameData.value.coefficient)
    }
}
