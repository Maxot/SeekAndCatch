package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.model.isFitForGoal
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock

class FigureRepositoryImplTest {

    private val colorsRepository: ColorsRepository = mock {
        on { selectedColors }.thenReturn(flowOf(setOf(androidx.compose.ui.graphics.Color.Red)))
    }
    private val testScope = TestScope()

    private val repository = FiguresRepositoryImpl(colorsRepository, testScope)

    private val goal = Goal.Shaped(Figure.FigureType.SQUARE)

    @Test
    fun getRandomFigures_halfSuitableFigures() {
        val figures = repository.getRandomFigures(
            itemsCount = 10,
            startId = 0,
            percentageOfSuitableGoalItems = 0.5f,
            goal = goal
        )
        var suitableFiguresCount = 0

        figures.forEach { figure ->
            if (figure.isFitForGoal(goal = goal)) suitableFiguresCount++
        }

        assertEquals(5, suitableFiguresCount)
    }
}