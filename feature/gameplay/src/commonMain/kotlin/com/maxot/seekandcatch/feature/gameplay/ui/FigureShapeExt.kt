package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.core.designsystem.RoundedTriangleShape
import com.maxot.seekandcatch.data.model.Figure

fun Figure.getShapeForFigure(): Shape {
    return when (this.type) {
        Figure.FigureType.TRIANGLE -> RoundedTriangleShape
        Figure.FigureType.CIRCLE -> CircleShape
        Figure.FigureType.SQUARE -> RoundedCornerShape(corner = CornerSize(10.dp))
    }
}
