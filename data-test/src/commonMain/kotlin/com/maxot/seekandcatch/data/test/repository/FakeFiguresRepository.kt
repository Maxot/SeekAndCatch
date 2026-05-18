package com.maxot.seekandcatch.data.test.repository

import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.FigureColor
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.model.isFitForGoal
import com.maxot.seekandcatch.data.repository.FiguresRepository

class FakeFiguresRepository() : FiguresRepository {

    private val randomFigure = Figure(type = Figure.FigureType.CIRCLE, color = FigureColor.Red)
    private var randomFigures = listOf<Figure>()
    override fun getRandomFigure(id: Int): Figure = randomFigure

    override fun getRandomFigures(itemsCount: Int): List<Figure> {
        return randomFigures
    }

    override fun getRandomFigures(
        itemsCount: Int,
        startId: Int,
        percentageOfSuitableGoalItems: Float,
        goal: Goal<Any>
    ): List<Figure> {
        if (randomFigures.isNotEmpty()) {
            return (0 until itemsCount).map { i ->
                val base = randomFigures[i % randomFigures.size]
                val isSuitable = (i < itemsCount * percentageOfSuitableGoalItems)
                if (isSuitable) {
                    base.copy(id = startId + i, type = goal.toFigureType() ?: base.type, color = goal.toColor() ?: base.color)
                } else {
                    val newFigure = base.copy(id = startId + i)
                    if (newFigure.isFitForGoal(goal)) {
                        newFigure.copy(type = if (goal is Goal.Shaped) Figure.FigureType.SQUARE else Figure.FigureType.TRIANGLE)
                    } else newFigure
                }
            }
        }
        return emptyList()
    }

    private fun Goal<Any>.toFigureType(): Figure.FigureType? = (this as? Goal.Shaped)?.getGoal()
    private fun Goal<Any>.toColor(): FigureColor? = (this as? Goal.Colored)?.getGoal()

    fun setRandomFigures(figures: List<Figure>) {
        randomFigures = figures
    }

    override fun getFigureSuitableForGoal(goal: Goal<Any>): Set<Figure> {
        return emptySet()
    }

    override fun getFigureUnsuitableForGoal(goal: Goal<Any>): Set<Figure> {
        return emptySet()
    }
}
