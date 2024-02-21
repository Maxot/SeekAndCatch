package com.maxot.seekandcatch.feature.gameplay.data

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape

data class Figure(
    val type: FigureType,
    val color: Color?,
)

sealed class FigureType {
    object Triangle : FigureType()
    object Square : FigureType()
    object Circle : FigureType()
}

fun Figure.getShapeForFigure(): Shape {
    return when (this.type) {
        FigureType.Triangle -> {
            GenericShape { size, _ ->
                // 1)
                moveTo(size.width / 2f, 0f)
                // 2)
                lineTo(size.width, size.height)
                // 3)
                lineTo(0f, size.height)
            }
        }
        FigureType.Circle -> {
            CircleShape
        }
        FigureType.Square -> {
            RectangleShape
        }
    }
}
