package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.getShapeForFigure

@Composable
fun ColoredFigureLayout(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    figure: Figure,
    onItemClick: () -> Unit = {}
) {
    var visible by remember {
        mutableStateOf(figure.isActive)
    }

    val alpha by animateFloatAsState(
        if (visible) 1f else 0f, label = "AlphaAnimation"
    )

    val colorState by remember {
        mutableStateOf(figure.color)
    }
    val shape: Shape = figure.getShapeForFigure()

    Box(
        modifier = Modifier
            .alpha(alpha)
            .then(modifier)
            .size(size)
            .padding(5.dp)
            .clip(shape)
            .background(colorState ?: Color.White)
            .border(width = 2.dp, color = Color.Black, shape = shape)
            .clickable {
                onItemClick()
                visible = false
            }

    )
}

@Preview
@Composable
fun ColoredFigureLayoutPreview() {
    ColoredFigureLayout(figure = Figure.getRandomFigure())
}