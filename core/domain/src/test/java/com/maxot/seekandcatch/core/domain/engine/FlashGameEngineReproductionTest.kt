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
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlashGameEngineReproductionTest {

    private lateinit var engine: FlashGameEngine
    private val figuresRepository = FakeFiguresRepository()
    private val goalsRepository = FakeGoalsRepository()
    private val testScope = TestScope()

    private val testGoal = Goal.Shaped(Figure.FigureType.CIRCLE)
    private val testFigures = (0 until 16).map { id ->
        Figure(
            id = id,
            type = Figure.FigureType.CIRCLE, // All are circles to make it easy to hit
            color = Color.Red
        )
    }

    private val gameParams = GameParams(
        itemsCount = 4,
        percentOfSuitableItem = 1.0f,
        coefficientStep = 0.1f,
        scorePoint = 10,
        maxLifeCount = 3,
        lifeCount = 3,
        rowWidth = 2,
        rowDuration = 1000
    )

    @Before
    fun setup() {
        figuresRepository.setRandomFigures(testFigures.take(4))
        goalsRepository.setRandomGoal(testGoal)
        engine = FlashGameEngine(testScope, figuresRepository, goalsRepository)
        engine.initGame(gameParams)
        testScope.testScheduler.advanceUntilIdle()
    }

    @Test
    fun reproduceFalseMiss_whenClickHappensJustAtFlashEnd() = testScope.runTest {
        engine.startGame()
        
        // baseSpawnPeriodMillis = (1000 * 1.2 * 1.5) = 1800
        // baseFlashMillis = (1000 * 2 * 1.2) = 2400
        
        advanceTimeBy(1800) // Spawn
        runCurrent()
        
        val visibleCells = engine.gameData.value.visibleCells.toSet()
        assertEquals("Should have 1 visible cell", 1, visibleCells.size)
        val firstVisible = visibleCells.first()
        
        // Advance time to just BEFORE the flash ends
        advanceTimeBy(2399) // Flash almost ends
        runCurrent()
        
        // Now we click. It should succeed because it's still visible
        engine.onItemClick(firstVisible)
        
        // Finish the flash
        advanceTimeBy(1)
        runCurrent()
        
        // 1 clicked, 0 missed.
        // Life should remain 3. Coefficient should be 1.1.
        assertEquals("Life count should be 3", 3, engine.gameData.value.lifeCount)
        assertEquals("Coefficient should be 1.1", 1.1f, engine.gameData.value.coefficient, 0.01f)
    }

    @Test
    fun reproduceFalseMiss_whenNoSuitableItemsAreFlashed() = testScope.runTest {
        // Goal is CIRCLE. Let's make all figures TRIANGLES
        val nonSuitableFigures = (0 until 4).map { id ->
            Figure(id = id, type = Figure.FigureType.TRIANGLE, color = Color.Red)
        }
        figuresRepository.setRandomFigures(nonSuitableFigures)
        
        // Re-init engine to pick up new figures
        engine = FlashGameEngine(testScope, figuresRepository, goalsRepository)
        engine.initGame(gameParams)
        testScope.testScheduler.advanceUntilIdle()

        engine.startGame()

        advanceTimeBy(1800) // First Spawn
        runCurrent()

        val visibleCells = engine.gameData.value.visibleCells.toSet()
        assertEquals("Should have 1 visible cell", 1, visibleCells.size)
        
        // Verify that the visible figure is NOT suitable
        visibleCells.forEach { index ->
            val figure = engine.gameData.value.figures[index]
            assertEquals("Figure at index $index should be TRIANGLE", Figure.FigureType.TRIANGLE, figure.type)
        }

        // Advance time to finish the flash
        advanceTimeBy(2400) 
        runCurrent()

        // No suitable items were flashed, so no penalty should occur.
        assertEquals("Life count should be 3", 3, engine.gameData.value.lifeCount)
        assertEquals("Coefficient should be 1.0", 1.0f, engine.gameData.value.coefficient, 0.01f)
        assertEquals("isLifeWasted should be false", false, engine.gameData.value.isLifeWasted)
        
        // Second flash cycle - let's make it a MIX of suitable and non-suitable
        // Note: we can't change figuresRepository on the fly easily because FlashGameEngine has its own copy in GameEngineData
        
        advanceTimeBy(1800) // Second Spawn
        runCurrent()
        
        advanceTimeBy(2400) // Second flash end
        runCurrent()

        assertEquals("Life count should still be 3", 3, engine.gameData.value.lifeCount)
        assertEquals("Coefficient should still be 1.0", 1.0f, engine.gameData.value.coefficient, 0.01f)
        assertEquals("isLifeWasted should still be false", false, engine.gameData.value.isLifeWasted)

        engine.finishGame()
    }

    @Test
    fun fixFalseMiss_whenAlreadyClickedItemsAreCleared() = testScope.runTest {
        // All 4 figures are circles (suitable)
        figuresRepository.setRandomFigures(testFigures.take(4))
        engine = FlashGameEngine(testScope, figuresRepository, goalsRepository)
        engine.initGame(gameParams)
        testScope.testScheduler.advanceUntilIdle()

        engine.startGame()

        // Click all items as they appear
        repeat(4) {
            advanceTimeBy(1801) // Spawn
            runCurrent()
            
            val visible = engine.gameData.value.visibleCells
            if (visible.isNotEmpty()) {
                engine.onItemClick(visible.first())
            }
            
            advanceTimeBy(2400) // End flash
            runCurrent()
        }
        
        // After 4 correct clicks, life count should still be 3.
        // If there were false misses, life count would have dropped.
        assertEquals("Life count should still be 3 after clearing all items", 3, engine.gameData.value.lifeCount)
        assertEquals("Game should be finished when all items are cleared", GameEngineState.Finished(engine.gameData.value.score), engine.gameState.value)
    }
}
