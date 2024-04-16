package com.maxot.seekandcatch.data.test.repository

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.repository.FiguresRepository

class FakeFiguresRepository() : FiguresRepository {

    private val randomFigure = Figure(type = Figure.FigureType.CIRCLE, color = Color.Red)
    private var randomFigures = listOf<Figure>()
    override fun getRandomFigure(): Figure = randomFigure

    override fun getRandomFigures(itemsCount: Int): List<Figure> {
        return randomFigures
    }

    override fun getRandomFigures(
        itemsCount: Int,
        percentageOfSuitableGoalItems: Float,
        goal: Goal<Any>
    ): List<Figure> {
        return randomFigures
    }

    /**
     * A test-only API to allow controlling the list of figures from tests.
     */
    fun setRandomFigures(figures: List<Figure>) {
        randomFigures = figures
    }
}