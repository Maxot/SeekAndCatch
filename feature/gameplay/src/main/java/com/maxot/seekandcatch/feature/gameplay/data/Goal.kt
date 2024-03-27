package com.maxot.seekandcatch.feature.gameplay.data

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.feature.gameplay.getRandomColor

sealed class Goal<out T : Any> {
    abstract fun getGoal(): T

    class Colored(private val value: Color) : Goal<Color>() {
        override fun getGoal(): Color {
            return value
        }
    }

    class Figured(private val figure: Figure) : Goal<Figure>() {
        override fun getGoal(): Figure {
            return figure
        }
    }

    companion object {
        fun getRandomGoal(seed: Int): Goal<Any> {
            return when (seed) {
                0 -> Colored(Color.getRandomColor())
                1 -> Figured(Figure(type = FigureType.getRandomFigureType(4), color = null))
//            2 -> Goal.Figured(Figure(getRandomFigureType(), getRandomColor()))
                else -> Colored(Color.getRandomColor())
            }
        }
    }
}
