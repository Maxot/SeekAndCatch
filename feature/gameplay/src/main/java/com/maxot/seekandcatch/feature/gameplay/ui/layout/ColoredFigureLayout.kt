package com.maxot.seekandcatch.feature.gameplay.ui.layout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.designsystem.component.drawCircleFigure
import com.maxot.seekandcatch.core.designsystem.component.drawSquareFigure
import com.maxot.seekandcatch.core.designsystem.component.drawTriangleFigure
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.getShapeForFigure
import com.maxot.seekandcatch.feature.gameplay.R

// Used for test
val AlphaKey = SemanticsPropertyKey<Float>("Alpha")
var SemanticsPropertyReceiver.alphaValue by AlphaKey

val ShapeKey = SemanticsPropertyKey<Shape>("Shape")
var SemanticsPropertyReceiver.shapeKey by ShapeKey

@Composable
fun ColoredFigureLayout(
    modifier: Modifier = Modifier,
    size: Dp? = null,
    figure: Figure,
    onItemClick: () -> Unit = {},
) {
    val coloredFigureContentDesc =
        stringResource(id = R.string.colored_figure_content_desc, figure.id)

    val shape: Shape = figure.getShapeForFigure()

    val color: Color = figure.color ?: Color.LightGray
    val secondColor: Color = Color.White

    val interactionSource = remember {
        MutableInteractionSource()
    }
    var heightInPx: Float = 0f

    val alpha by animateFloatAsState(
        if (figure.isActive) 1f else 0f, label = "AlphaAnimation"
    )
    val alphaScore = remember { Animatable(1f) }

    Box(
        modifier = Modifier
            .semantics {
                contentDescription = coloredFigureContentDesc
                alphaValue = alpha // TODO: used for test, need to find better way to test alpha
                shapeKey = shape
            }
            .padding(10.dp)
            .run { size?.let { size(size) } ?: aspectRatio(1f) }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = figure.isActive
            ) {
                onItemClick()
            }
            .alpha(alpha)
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sizePx = size?.toPx() ?: this.size.minDimension
            when (figure.type) {
                Figure.FigureType.SQUARE -> drawSquareFigure(sizePx, color)
                Figure.FigureType.CIRCLE -> drawCircleFigure(sizePx, color)
                Figure.FigureType.TRIANGLE -> drawTriangleFigure(sizePx, color)
            }
        }

        if (figure.pointsReceived != null) {
            LaunchedEffect(key1 = true) {
                alphaScore.animateTo(targetValue = 0f, animationSpec = tween(1000))
            }
            Text(
                text = "+${figure.pointsReceived}",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.alpha(alphaScore.value),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
fun ColoredFigureLayoutActivePreview() {
    SeekAndCatchTheme {
        ColoredFigureLayout(figure = Figure(type = Figure.FigureType.TRIANGLE, color = Color.Red))
    }
}

@Preview
@Composable
fun ColoredFigureLayoutNotActivePreview() {
    SeekAndCatchTheme {
        ColoredFigureLayout(
            figure = Figure(
                type = Figure.FigureType.TRIANGLE,
                color = Color.Red,
                isActive = false
            )
        )
    }
}


@Preview
@Composable
fun SquareFigurePreview() {
    SeekAndCatchTheme {
        ColoredFigureLayout(
            modifier = Modifier.size(96.dp),
            figure = Figure(
                type = Figure.FigureType.SQUARE,
                color = Color.Blue,
                isActive = true
            )
        )
    }
}

@Preview
@Composable
fun CircleFigurePreview() {
    SeekAndCatchTheme {
        ColoredFigureLayout(
            modifier = Modifier.size(96.dp),
            figure = Figure(
                type = Figure.FigureType.CIRCLE,
                color = Color.Green,
                isActive = true
            )
        )
    }
}

@Preview
@Composable
fun TriangleFigurePreview() {
    SeekAndCatchTheme {
        ColoredFigureLayout(
            modifier = Modifier.size(96.dp),
            figure = Figure(
                type = Figure.FigureType.TRIANGLE,
                color = Color.Red,
                isActive = true
            )
        )
    }
}
