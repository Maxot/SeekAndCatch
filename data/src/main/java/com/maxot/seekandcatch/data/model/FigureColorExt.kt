package com.maxot.seekandcatch.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun FigureColor.toComposeColor(): Color = Color(argb)

fun Color.toFigureColor(): FigureColor = FigureColor(toArgb())
