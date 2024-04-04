package com.maxot.seekandcatch.feature.gameplay.data

import androidx.compose.ui.graphics.Color

/**
 * Represent goal in game process. Used to determine if user click on correct object in game.
 */
sealed class Goal<out T : Any> {
    abstract fun getGoal(): T

    class Colored(private val value: Color) : Goal<Color>() {
        override fun getGoal(): Color {
            return value
        }
    }

    class Shaped(private val value: Figure.FigureType) : Goal<Figure.FigureType>() {
        override fun getGoal(): Figure.FigureType {
            return value
        }
    }

    companion object {

        fun getRandomGoal(seed: Int): Goal<Any> {
            return when (seed) {
                0 -> Colored(Figure.getRandomColor())
                1 -> Shaped(Figure.FigureType.getRandomFigureType())
                else -> Colored(Figure.getRandomColor())
            }
        }
    }
}

fun Goal<Any>.getSuitableFigures(): Set<Figure> {
    val figures = mutableSetOf<Figure>()
    when (this) {
        is Goal.Colored -> {
            val goalColor = getGoal()
            Figure.FigureType.entries.forEach { type ->
                val coloredFigure = Figure(type = type, color = goalColor)
                figures.add(coloredFigure)
            }
        }

        is Goal.Shaped -> {
            val goalFigureType = getGoal()
            Figure.availableColors.forEach { color ->
                val shapedFigure = Figure(type = goalFigureType, color = color)
                figures.add(shapedFigure)
            }
        }
    }
    return figures
}

fun Goal<Any>.getUnsuitableFigures(): Set<Figure> {
    val figures = mutableSetOf<Figure>()
    when (this) {
        is Goal.Colored -> {
            val goalColor = getGoal()
            Figure.FigureType.entries.forEach { type ->
                Figure.availableColors.forEach { color ->
                    if (goalColor != color) {
                        val coloredFigure = Figure(type = type, color = color)
                        figures.add(coloredFigure)
                    }
                }
            }
        }

        is Goal.Shaped -> {
            val goalFigureType: Figure.FigureType = getGoal()
            Figure.availableColors.forEach { color ->
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
