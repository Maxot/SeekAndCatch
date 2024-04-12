package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.model.getSuitableFigures
import com.maxot.seekandcatch.data.model.getUnsuitableFigures
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
        percentageOfSuitableGoalItems: Float,
        goal: Goal<Any>
    ): List<Figure> {
        return generateFiguresWithSelectedAndOtherItems(
            itemsCount = itemsCount,
            percentOfSelectedItems = percentageOfSuitableGoalItems,
            selectedItems = goal.getSuitableFigures(),
            otherItems = goal.getUnsuitableFigures()
        )
    }

    private fun generateFiguresWithSelectedAndOtherItems(
        itemsCount: Int,
        percentOfSelectedItems: Float,
        selectedItems: Set<Figure>,
        otherItems: Set<Figure>
    ): List<Figure> {
        val result = mutableListOf<Figure>()
        val selectedItemsCount = (percentOfSelectedItems * itemsCount).toInt()

        for (i in 0..<selectedItemsCount) {
            val randomFigure = selectedItems.random()
            result.add(
                Figure(
                    id = i,
                    type = randomFigure.type,
                    color = randomFigure.color
                )
            )
        }
        for (i in selectedItemsCount..<itemsCount) {
            val randomFigure = otherItems.random()
            result.add(
                Figure(
                    id = i,
                    type = randomFigure.type,
                    color = randomFigure.color
                )
            )
        }

        return result.shuffled()
    }
}
