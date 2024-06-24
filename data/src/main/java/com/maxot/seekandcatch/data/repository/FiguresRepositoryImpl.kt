package com.maxot.seekandcatch.data.repository

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.core.common.di.ApplicationScope
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class FiguresRepositoryImpl
@Inject constructor(
    private val colorsRepository: ColorsRepository,
    @ApplicationScope private val coroutineScope: CoroutineScope
) : FiguresRepository {
    private val availableColors = mutableSetOf<Color>()

    init {
        coroutineScope.launch {
            colorsRepository.selectedColors.collect {
                availableColors.clear()
                availableColors.addAll(it)
            }
        }
    }

    override fun getRandomFigure(id: Int): Figure {
        val figureType = Figure.FigureType.entries.random()
        val color = availableColors.random()
        return Figure(id = id, type = figureType, color = color)
    }

    override fun getRandomFigures(itemsCount: Int): List<Figure> {
        val listOfFigures = mutableListOf<Figure>()
        repeat(itemsCount) { i ->
            listOfFigures.add(getRandomFigure(id = i))
        }
        return listOfFigures
    }

    override fun getRandomFigures(
        itemsCount: Int,
        startId: Int,
        percentageOfSuitableGoalItems: Float,
        goal: Goal<Any>
    ): List<Figure> {
        return generateFiguresWithSelectedAndOtherItems(
            itemsCount = itemsCount,
            startId = startId,
            percentOfSelectedItems = percentageOfSuitableGoalItems,
            selectedItems = getFigureSuitableForGoal(goal),
            otherItems = getFigureUnsuitableForGoal(goal)
        )
    }

    private fun generateFiguresWithSelectedAndOtherItems(
        itemsCount: Int,
        startId: Int = 0,
        percentOfSelectedItems: Float,
        selectedItems: Set<Figure>,
        otherItems: Set<Figure>
    ): List<Figure> {
        val result = mutableListOf<Figure>()
        val selectedItemsCount = (percentOfSelectedItems * itemsCount).toInt()

        for (i in startId..<selectedItemsCount + startId) {
            val randomFigure = selectedItems.random()
            result.add(
                Figure(
                    id = i,
                    type = randomFigure.type,
                    color = randomFigure.color
                )
            )
        }
        for (i in startId + selectedItemsCount..<startId + itemsCount) {
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


    /**
     * Return all possible [Figure] that are fit for the current [Goal]
     */
    private fun getFigureSuitableForGoal(goal: Goal<Any>): Set<Figure> {
        val figures = mutableSetOf<Figure>()
        when (goal) {
            is Goal.Colored -> {
                val goalColor = goal.getGoal()
                Figure.FigureType.entries.forEach { type ->
                    val coloredFigure = Figure(type = type, color = goalColor)
                    figures.add(coloredFigure)
                }
            }

            is Goal.Shaped -> {
                val goalFigureType = goal.getGoal()
                availableColors.forEach { color ->
                    val shapedFigure = Figure(type = goalFigureType, color = color)
                    figures.add(shapedFigure)
                }
            }
        }
        return figures
    }

    /**
     * Return all possible [Figure] that are not fit for the current [Goal]
     */
    private fun getFigureUnsuitableForGoal(goal: Goal<Any>): Set<Figure> {
        val figures = mutableSetOf<Figure>()
        when (goal) {
            is Goal.Colored -> {
                val goalColor = goal.getGoal()
                Figure.FigureType.entries.forEach { type ->
                    availableColors.forEach { color ->
                        if (goalColor != color) {
                            val coloredFigure = Figure(type = type, color = color)
                            figures.add(coloredFigure)
                        }
                    }
                }
            }

            is Goal.Shaped -> {
                val goalFigureType: Figure.FigureType = goal.getGoal()
                availableColors.forEach { color ->
                    Figure.FigureType.entries.forEach { type ->
                        if (goalFigureType != type) {
                            val shapedFigure = Figure(type = type, color = color)
                            figures.add(shapedFigure)
                        }
                    }
                }
            }
        }
        return figures
    }

}
