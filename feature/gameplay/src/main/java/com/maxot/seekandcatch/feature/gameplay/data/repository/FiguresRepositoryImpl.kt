package com.maxot.seekandcatch.feature.gameplay.data.repository

import com.maxot.seekandcatch.feature.gameplay.data.Figure
import com.maxot.seekandcatch.feature.gameplay.data.Goal
import com.maxot.seekandcatch.feature.gameplay.data.getSuitableFigures
import com.maxot.seekandcatch.feature.gameplay.data.getUnsuitableFigures
import javax.inject.Inject

class FiguresRepositoryImpl
@Inject constructor() : FiguresRepository {

    override fun getRandomFigure(): Figure {
        return Figure.getRandomFigure()
    }

    override fun getRandomFigures(itemsCount: Int): List<Figure> {
        val listOfFigures = mutableListOf<Figure>()
        for (i in 0..<itemsCount) {
            listOfFigures.add(Figure.getRandomFigure(id = i))
        }
        return listOfFigures
    }

    override fun getRandomFigures(
        itemsCount: Int,
        percentOfGoalSuitedItems: Float,
        goal: Goal<Any>
    ): List<Figure> {
        return getFiguresWithLoadOfSelectedItems(
            itemsCount = itemsCount,
            percentOfSelectedItems = percentOfGoalSuitedItems,
            selectedItems = goal.getSuitableFigures(),
            otherItems = goal.getUnsuitableFigures()
        )
    }

    private fun getFiguresWithLoadOfSelectedItems(
        itemsCount: Int,
        percentOfSelectedItems: Float,
        selectedItems: Set<Figure>,
        otherItems: Set<Figure>
    ): List<Figure> {
        val result = mutableListOf<Figure>()
        val selectedItemsCount = (percentOfSelectedItems * itemsCount).toInt()

        for (i in 0..<selectedItemsCount) {
            val randomFigure = selectedItems.random()
            result.add(Figure(i, type = randomFigure.type, color = randomFigure.color))
        }
        for (i in selectedItemsCount..<itemsCount) {
            val randomFigure = otherItems.random()
            result.add(Figure(id = i, type = randomFigure.type, color = randomFigure.color))
        }

        return result.shuffled()
    }
}
