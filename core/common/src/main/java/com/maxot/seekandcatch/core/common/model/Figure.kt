package com.maxot.seekandcatch.core.common.model

import androidx.compose.ui.graphics.Color

data class Figure(
    val id: Int = 0,
    val type: FigureType,
    val color: Color? = null,
    var isActive: Boolean = true,
    var pointsReceived: Int? = null
) {
    companion object {
        fun getRandomFigure(
            id: Int = 0,
            availableColors: Set<Color> = setOf(
                Color.Red,
                Color.Blue,
                Color.Green,
                Color.Yellow
            )
        ): Figure {
            val figureType = FigureType.entries.random()
            val color = availableColors.random()
            return Figure(id = id, type = figureType, color = color)
        }
    }

    enum class FigureType {
        TRIANGLE,
        SQUARE,
        CIRCLE;

        companion object {
            fun getRandomFigureType(): FigureType {
                return FigureType.entries.random()
            }
        }
    }

}

fun Figure.isFitForGoal(goal: Goal<Any>): Boolean {
    return when (goal) {
        is Goal.Colored -> {
            goal.getGoal() == this.color
        }

        is Goal.Shaped -> {
            goal.getGoal() == this.type
        }
    }
}
