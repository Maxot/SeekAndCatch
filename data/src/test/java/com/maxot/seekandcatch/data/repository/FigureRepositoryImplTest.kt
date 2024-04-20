package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.model.isFitForGoal
import org.junit.Assert.assertEquals
import org.junit.Test

class FigureRepositoryImplTest {

    private val repository = FiguresRepositoryImpl()

    private val goal = Goal.Shaped(Figure.FigureType.SQUARE)

    @Test
    fun getRandomFigures_halfSuitableFigures() {
        val figures = repository.getRandomFigures(10, 0.5f, goal)
        var suitableFiguresCount = 0

        figures.forEach { figure ->
            if (figure.isFitForGoal(goal = goal)) suitableFiguresCount++
        }

        assertEquals(5, suitableFiguresCount)
    }
}