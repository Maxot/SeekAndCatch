package com.maxot.seekandcatch.data

import androidx.compose.ui.graphics.Color

data class Figure(
    val type: FigureType,
    var color: Color,
)

sealed class FigureType {
    object Triangle : FigureType()
    object Square : FigureType()
    object Circle : FigureType()
}
