package com.maxot.seekandcatch.core.domain.engine

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.core.common.model.GameParams
import com.maxot.seekandcatch.core.common.model.Figure
import com.maxot.seekandcatch.core.common.model.Goal
import com.maxot.seekandcatch.core.common.model.isFitForGoal
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
    fun gameShouldNotFinish_whenNoSuitableItemsExist() = testScope.runTest {
        // Goal is CIRCLE. Let's make all figures TRIANGLES (none are suitable)
        val nonSuitableFigures = (0 until 4).map { id ->
            Figure(id = id, type = Figure.FigureType.TRIANGLE, color = Color.Red)
        }
        figuresRepository.setRandomFigures(nonSuitableFigures)
        
        // Re-init engine to pick up new figures
        engine = FlashGameEngine(testScope, figuresRepository, goalsRepository)
        engine.initGame(gameParams)
        testScope.testScheduler.advanceUntilIdle()

        engine.startGame()

        advanceTimeBy(1800) // First Spawn attempts
        runCurrent()

        // It should NOT have finished. It should have generated a suitable item.
        assertTrue("Game should NOT be finished", 
            engine.gameState.value is GameEngineState.Started)
        
        val visibleCells = engine.gameData.value.visibleCells
        assertTrue("Visible cells should not be empty", visibleCells.isNotEmpty())
        
        val figures = engine.gameData.value.figures
        val goals = engine.gameData.value.goals
        val hasSuitable = visibleCells.any { index -> figures[index].isFitForGoal(goals.first()) }
        assertTrue("Should have at least one suitable item in visible cells", hasSuitable)
    }


    @Test
    fun eachFlashCycle_shouldContainAtLeastOneSuitableItem() = testScope.runTest {
        val mixedFigures = listOf(
            Figure(id = 0, type = Figure.FigureType.CIRCLE, color = Color.Red), // Suitable
            Figure(id = 1, type = Figure.FigureType.SQUARE, color = Color.Red), // Unsuitable
            Figure(id = 2, type = Figure.FigureType.SQUARE, color = Color.Red), // Unsuitable
            Figure(id = 3, type = Figure.FigureType.SQUARE, color = Color.Red)  // Unsuitable
        )
        figuresRepository.setRandomFigures(mixedFigures)
        
        // 100 lives to avoid dying from misses
        val manyLivesParams = gameParams.copy(maxLifeCount = 100, lifeCount = 100)
        
        engine = FlashGameEngine(testScope, figuresRepository, goalsRepository)
        engine.initGame(manyLivesParams)
        testScope.testScheduler.advanceUntilIdle()

        engine.startGame()
        
        repeat(10) {
            advanceTimeBy(1801) // Wait for spawn
            runCurrent()
            
            val currentData = engine.gameData.value
            val visibleIndices = currentData.visibleCells
            val figures = currentData.figures
            val goals = currentData.goals
            
            assertTrue("Iteration $it: Visible cells should not be empty", visibleIndices.isNotEmpty())

            val suitableVisible = visibleIndices.filter { index ->
                val figure = figures[index]
                goals.any { figure.isFitForGoal(it) }
            }
            
            assertTrue("Iteration $it: Should have at least one suitable visible item, but got: $visibleIndices", 
                suitableVisible.isNotEmpty())
            
            advanceTimeBy(2399) // Wait until almost end of flash
            runCurrent()
        }
    }
}
