package com.maxot.seekandcatch.data.model

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import kotlin.random.Random

/**
 * Represent an object(figure) that is main block of the game and used in game process.
 */
data class Figure(
    val id: Int = 0,
    val type: FigureType,
    val color: Color? = null,
    var isActive: Boolean = true
) {
    companion object {
        val availableColors = arrayOf(Color.Red, Color.Blue, Color.Green, Color.Yellow)
        fun getRandomFigure(id: Int = 0): Figure {
            val figureType = FigureType.entries.random()
            val color = availableColors.random()
            return Figure(id = id, type = figureType, color = color)
        }

        fun getRandomColor() = availableColors.random()
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


fun Figure.getShapeForFigure(): Shape {
    return when (this.type) {
        Figure.FigureType.TRIANGLE -> {
            GenericShape { size, _ ->
                // 1)
                moveTo(size.width / 2f, 0f)
                // 2)
                lineTo(size.width, size.height)
                // 3)
                lineTo(0f, size.height)
            }
        }

        Figure.FigureType.CIRCLE -> {
            CircleShape
        }

        Figure.FigureType.SQUARE -> {
            RectangleShape
        }
    }
}

/**
 * Check if the [Figure] is fit for the [Goal].
 */
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
