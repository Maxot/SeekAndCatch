package com.maxot.seekandcatch.core.domain.engine

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.core.common.model.GameParams
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.test.repository.FakeFiguresRepository
import com.maxot.seekandcatch.data.test.repository.FakeGoalsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlowGameEngineTest {

    private lateinit var engine: FlowGameEngine
    private val figuresRepository = FakeFiguresRepository()
    private val goalsRepository = FakeGoalsRepository()
    private val testScope = TestScope()

    private val testGoal = Goal.Shaped(Figure.FigureType.CIRCLE)
    private val testFigures = (0 until 20).map { id ->
        Figure(
            id = id,
            type = if (id % 2 == 0) Figure.FigureType.CIRCLE else Figure.FigureType.TRIANGLE,
            color = Color.Red
        )
    }

    private val gameParams = GameParams(
        itemsCount = 20,
        percentOfSuitableItem = 0.5f,
        coefficientStep = 0.5f,
        scorePoint = 10,
        maxLifeCount = 5,
        lifeCount = 3,
        rowWidth = 4,
        rowDuration = 1000
    )

    @Before
    fun setup() {
        figuresRepository.setRandomFigures(testFigures)
        goalsRepository.setRandomGoal(testGoal)
        engine = FlowGameEngine(testScope, figuresRepository, goalsRepository)
        engine.initGame(gameParams)
        testScope.testScheduler.advanceUntilIdle()
    }

    @Test
    fun scroll_processesPassedItems_andDecreasesLifeIfMissed() {
        engine.startGame()
        testScope.testScheduler.advanceUntilIdle()
        // Row width is 4. Items 0, 1, 2, 3 are in the first row.
        // Item 0 is CIRCLE (suitable), 1 is TRIANGLE, 2 is CIRCLE (suitable), 3 is TRIANGLE.
        // If we scroll past them without clicking, life should decrease twice.
        
        // setFirstVisibleItemIndex triggers processing of items (index - rowWidth * 2)
        // To process row 0 (indices 0-3), we need firstVisibleItemIndex to be at least rowWidth * 2 + rowWidth = 12
        engine.setFirstVisibleItemIndex(12)
        
        // Suitable items in row 0: id 0 and id 2. Both missed.
        // LifeCount started at 3. 3 -> 2 -> 1.
        assertEquals(1, engine.gameData.value.lifeCount)
    }

    @Test
    fun scroll_missedItem_decreasesCoefficientFirst() {
        engine.startGame()
        testScope.testScheduler.advanceUntilIdle()
        // Increase coefficient first
        engine.onItemClick(0) // coef 1.5
        assertEquals(1.5f, engine.gameData.value.coefficient, 0.01f)
        
        // Now miss an item in row 1 (indices 4-7). Suitable is id 4 (even), id 6 (even).
        // Let's miss id 4. Row 1 is indices 4-7.
        // To process row 1, we need firstVisibleItemIndex to be at least rowWidth * 2 + 2 * rowWidth = 16
        engine.setFirstVisibleItemIndex(16)
        
        // Row 1 has id 4 (CIRCLE, matches) and id 6 (CIRCLE, matches).
        // Both are missed.
        // First miss: coef 1.5 -> 1.0 (clamped).
        // Second miss: coef is 1.0, so HP 3 -> 2.
        assertEquals(1.0f, engine.gameData.value.coefficient, 0.01f)
        assertEquals(2, engine.gameData.value.lifeCount)
    }

    @Test
    fun scroll_missedItem_hpReachesZero_gameOver() {
        val customParams = gameParams.copy(lifeCount = 1)
        engine.initGame(customParams)
        testScope.testScheduler.advanceUntilIdle()
        engine.startGame()
        
        // Row 0 has 2 suitable items (0, 2).
        // Miss them.
        engine.setFirstVisibleItemIndex(12)
        
        // First miss: HP 1 -> 0, Game Over.
        // Second miss: engine is finished, no more changes.
        assertEquals(0, engine.gameData.value.lifeCount)
        assertTrue(engine.gameState.value is GameEngineState.Finished)
    }

    @Test
    fun flow_lifeRecovery_atThreshold() {
        val customParams = gameParams.copy(lifeCount = 2, maxLifeCount = 5, itemsPassedWithoutMissToGetLife = 2)
        engine.initGame(customParams)
        testScope.testScheduler.advanceUntilIdle()
        engine.startGame()
        
        // Item 0 is CIRCLE (suitable), Item 2 is CIRCLE (suitable)
        engine.onItemClick(0)
        assertEquals(2, engine.gameData.value.lifeCount)
        
        engine.onItemClick(2) // 2nd correct tap, matches threshold
        assertEquals(3, engine.gameData.value.lifeCount)
    }

    @Test
    fun scroll_approachingEnd_addsMoreItems() {
        engine.startGame()
        val initialSize = engine.gameData.value.figures.size // 20
        
        // Trigger approach end (index > size - 100)
        // Since size is 20, any index > -80 will trigger it.
        engine.setFirstVisibleItemIndex(10)
        testScope.testScheduler.advanceUntilIdle()
        
        assertTrue(engine.gameData.value.figures.size > initialSize)
    }

    @Test
    fun calculateScrollDuration_changesWithCoefficient() {
        engine.startGame()
        val initialDuration = engine.gameData.value.scrollDuration
        
        // Increase coefficient
        engine.onItemClick(0)
        engine.onItemClick(2)
        // Coefficient should be 2.0f
        
        val newDuration = engine.gameData.value.scrollDuration
        assertTrue("Duration should decrease as coefficient increases. Initial: $initialDuration, New: $newDuration", 
            newDuration < initialDuration)
    }

    @Test
    fun calculatePixelsToScroll_dependsOnItemHeight() {
        engine.startGame()
        engine.setItemHeight(200)
        
        val rowCount = 20 / 4 // 5 rows
        val expectedPixels = 5 * 200f
        assertEquals(expectedPixels, engine.gameData.value.pixelsToScroll, 0.01f)
    }
}
