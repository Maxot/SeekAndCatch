package com.maxot.seekandcatch.core.domain.engine

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.core.common.model.GameParams
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.test.repository.FakeFiguresRepository
import com.maxot.seekandcatch.data.test.repository.FakeGoalsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnItemClickTest {

    private lateinit var engine: TestGameEngine
    private val figuresRepository = FakeFiguresRepository()
    private val goalsRepository = FakeGoalsRepository()
    private val testScope = TestScope()

    private val testGoal = Goal.Shaped(Figure.FigureType.CIRCLE)
    private val testFigures = listOf(
        Figure(id = 0, type = Figure.FigureType.CIRCLE, color = Color.Red),
        Figure(id = 1, type = Figure.FigureType.TRIANGLE, color = Color.Blue)
    )

    class TestGameEngine(
        scope: TestScope,
        figuresRepo: FakeFiguresRepository,
        goalsRepo: FakeGoalsRepository
    ) : BaseGameEngine(scope, figuresRepo, goalsRepo) {
        override fun setFirstVisibleItemIndex(index: Int) {}
        override fun setItemHeight(height: Int) {}
        
        fun setState(state: GameEngineState) {
            _gameState.value = state
        }
    }

    @Before
    fun setup() {
        figuresRepository.setRandomFigures(testFigures)
        goalsRepository.setRandomGoal(testGoal)
        engine = TestGameEngine(testScope, figuresRepository, goalsRepository)
        engine.initGame(GameParams(itemsCount = 2))
        testScope.testScheduler.advanceUntilIdle()
    }

    @Test
    fun onItemClick_worksInStartedState() {
        engine.startGame()
        engine.onItemClick(0) // Correct tap
        // Streak is 10 by default, so coefficient doesn't change
        assertEquals(10, engine.gameData.value.score)
    }

    @Test
    fun onItemClick_ignoredInCreatedState() {
        engine.setState(GameEngineState.Created(emptySet()))
        engine.onItemClick(0)
        assertEquals(0, engine.gameData.value.score)
    }

    @Test
    fun onItemClick_ignoresInactiveFigure() {
        engine.startGame()
        // Manually deactivate figure
        engine.onItemClick(0)
        assertEquals(10, engine.gameData.value.score)
        
        engine.onItemClick(0) // Try clicking again
        assertEquals(10, engine.gameData.value.score) // Score should not increase
    }
}
