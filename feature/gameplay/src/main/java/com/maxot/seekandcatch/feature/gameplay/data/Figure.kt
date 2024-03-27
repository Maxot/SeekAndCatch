package com.maxot.seekandcatch.feature.gameplay.data

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import com.maxot.seekandcatch.feature.gameplay.getRandomColor
import kotlin.random.Random

data class Figure(
    val id: Int = 0,
    val type: FigureType,
    val color: Color?,
    var isActive: Boolean = true
) {
    companion object {
        fun getRandomFigure(id: Int = 0): Figure {
            val figureType = FigureType.getRandomFigureType(Random.nextInt(4))
            val color = Color.getRandomColor(Random.nextInt(4))
            return Figure(id = id, type = figureType, color = color)
        }
    }
}

enum class FigureType {
    TRIANGLE,
    SQUARE,
    CIRCLE;

    companion object {
        fun getRandomFigureType(seed: Int): FigureType {
            return when (seed) {
                0 -> CIRCLE
                1 -> SQUARE
                2 -> TRIANGLE
                else -> CIRCLE
            }
        }
    }
}

fun Figure.getShapeForFigure(): Shape {
    return when (this.type) {
        FigureType.TRIANGLE -> {
            GenericShape { size, _ ->
                // 1)
                moveTo(size.width / 2f, 0f)
                // 2)
                lineTo(size.width, size.height)
                // 3)
                lineTo(0f, size.height)
            }
        }

        FigureType.CIRCLE -> {
            CircleShape
        }

        FigureType.SQUARE -> {
            RectangleShape
        }
    }
}
