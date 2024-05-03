package com.maxot.seekandcatch.feature.gameplay.ui.layout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
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
    onItemClick: () -> Unit = {}
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
    var heightInPx: Float = 0f

    var visible by remember {
        mutableStateOf(figure.isActive)
    }

    val alpha by animateFloatAsState(
        if (visible) 1f else 0f, label = "AlphaAnimation"
    )

    Box(
        modifier = Modifier
            .semantics {
                contentDescription = coloredFigureContentDesc
                alphaValue = alpha // TODO: used for test, need to find better way to test alpha
                shapeKey = shape
            }
            .run { size?.let { size(size) } ?: aspectRatio(1f) }
            .alpha(alpha)
            .padding(10.dp)
            .clip(shape)
            .border(width = 1.dp, color = Color.Gray, shape = shape)
            .background(largeRadialGradient)
            .onGloballyPositioned {
                heightInPx = it.size.height.toFloat() // Maybe return from there?
            }
            .clickable(visible) {
                onItemClick()
                visible = false
            }
            .then(modifier)
    )
}

@Preview
@Composable
fun ColoredFigureLayoutPreview() {
    SeekAndCatchTheme {
        ColoredFigureLayout(figure = Figure.getRandomFigure())
    }
}