package com.maxot.seekandcatch.feature.gameplay.ui.layout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
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
    onItemClick: () -> Int = { 0 }
) {
    val coloredFigureContentDesc =
        stringResource(id = R.string.colored_figure_content_desc, figure.id)

    val shape: Shape = figure.getShapeForFigure()

    val color: Color = figure.color ?: Color.LightGray
    val secondColor: Color = Color.White
    val backgroundBrush = Brush.radialGradient(listOf(secondColor, color))
    val largeRadialGradient = object : ShaderBrush() {
        override fun createShader(size: Size): Shader {
            val biggerDimension = maxOf(size.height, size.width)
            return RadialGradientShader(
                colors = listOf(secondColor, color),
                center = size.center,
                radius = biggerDimension / 2f,
                colorStops = listOf(0f, 0.95f)
            )
        }
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }
    var heightInPx: Float = 0f

    val alpha by animateFloatAsState(
        if (figure.isActive) 1f else 0f, label = "AlphaAnimation"
    )
    val alphaScore = remember { Animatable(1f) }

    val pointsAdded = remember {
        mutableStateOf(0)
    }
    Box(
        modifier = Modifier
            .semantics {
                contentDescription = coloredFigureContentDesc
                alphaValue = alpha // TODO: used for test, need to find better way to test alpha
                shapeKey = shape
            }
            .run { size?.let { size(size) } ?: aspectRatio(1f) }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = figure.isActive
            ) {
                pointsAdded.value = onItemClick()
            }
            .alpha(alpha)
            .padding(10.dp)
            .clip(shape)
            .border(width = 1.dp, color = Color.Gray, shape = shape)
            .background(largeRadialGradient)
            .onGloballyPositioned {
                heightInPx = it.size.height.toFloat() // Maybe return from there?
            }
            .then(modifier)
    )

    if (!figure.isActive) {
        LaunchedEffect(key1 = true) {
            alphaScore.animateTo(targetValue = 0f, animationSpec = tween(1000))
        }
        Box(
            modifier = Modifier
                .run { size?.let { size(size) } ?: aspectRatio(1f) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+${pointsAdded.value}",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier
                    .alpha(alphaScore.value),
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

