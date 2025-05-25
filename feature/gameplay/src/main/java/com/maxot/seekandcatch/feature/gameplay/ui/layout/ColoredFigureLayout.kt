package com.maxot.seekandcatch.feature.gameplay.ui.layout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
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
    onItemClick: () -> Unit = {},
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
                onItemClick()
            }
            .alpha(alpha)
            .padding(10.dp)
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
//        if (figure.type == Figure.FigureType.GENIUS_PRO){
//            Image(
//                painter = painterResource(com.maxot.seekandcatch.core.designsystem.R.drawable.genius_pro),
//                contentDescription = " ",
//                modifier = Modifier.border(2.dp, figure.color!!, shape = CircleShape)
//            )
//        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawPixelFigure(
                    size = size?.toPx() ?: this.size.minDimension,
                    figure = figure
                )
            }
//        }

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

fun DrawScope.drawPixelFigure(size: Float, figure: Figure) {
    // 10x10 grid, retro pixel-art style, with highlight/shadow, enhanced contrast
    val pixelSize = size / 10f
    val baseColor = figure.color ?: Color(0xFFCCCCCC)
    // Enhance retro contrast for highlight/shadow
    fun saturate(color: Color, factor: Float): Color {
        val r = color.red
        val g = color.green
        val b = color.blue
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val l = (max + min) / 2f
        val d = max - min
        val s = if (d == 0f) 0f else d / (1f - Math.abs(2 * l - 1f))
        val newS = (s * factor).coerceIn(0f, 1f)
        val h = when {
            d == 0f -> 0f
            max == r -> 60f * (((g - b) / d) % 6f)
            max == g -> 60f * (((b - r) / d) + 2f)
            else -> 60f * (((r - g) / d) + 4f)
        }
        // Simple desaturate for shadow, saturate for highlight
        // Use HSL to RGB conversion
        val c = (1f - Math.abs(2 * l - 1f)) * newS
        val x = c * (1f - Math.abs((h / 60f) % 2 - 1f))
        val m = l - c / 2f
        val (r1, g1, b1) = when {
            h < 0f -> Triple(0f, 0f, 0f)
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            h <= 360f -> Triple(c, 0f, x)
            else -> Triple(0f, 0f, 0f)
        }
        return Color((r1 + m).coerceIn(0f, 1f), (g1 + m).coerceIn(0f, 1f), (b1 + m).coerceIn(0f, 1f), color.alpha)
    }
    val highlightColor = Color.White
    val shadowColor = Color(0xFF191919)
    val darkShadowColor = Color(0xFF000000)
    val lightHighlight = saturate(baseColor, 1.7f).copy(alpha = 1f).compositeOver(Color.White.copy(alpha = 0.25f))
    val darkShadow = saturate(baseColor, 0.3f).copy(alpha = 1f).compositeOver(Color.Black.copy(alpha = 0.22f))
    fun pixel(x: Int, y: Int, color: Color) {
        drawRect(
            color = color,
            topLeft = Offset(x * pixelSize, y * pixelSize),
            size = Size(pixelSize, pixelSize)
        )
    }

    when (figure.type) {
        Figure.FigureType.SQUARE -> {
            // 8x8 solid square at (1..8, 1..8), retro style, highlight left/top, shadow right/bottom
            for (x in 1..8) for (y in 1..8) pixel(x, y, baseColor)
            // highlight: top edge and left edge
            for (x in 1..8) pixel(x, 1, lightHighlight)
            for (y in 1..8) pixel(1, y, lightHighlight)
            // shadow: right edge and bottom edge
            for (y in 1..8) pixel(8, y, darkShadow)
            for (x in 1..8) pixel(x, 8, darkShadow)
            // extra highlight at (2,2) for a retro pixel shine
            pixel(2, 2, highlightColor)
            // extra shadow at (7,7) for retro pixel shadow
            pixel(7, 7, darkShadowColor)
        }
        Figure.FigureType.CIRCLE -> {
            // Wider 10x10 round cluster, with highlight/shadow
            // Center: (5,5), hand-picked for pixel-art look, fit tightly in 10x10 grid
            val main = listOf(
                3 to 1, 4 to 1, 5 to 1, 6 to 1,
                2 to 2, 3 to 2, 4 to 2, 5 to 2, 6 to 2, 7 to 2,
                1 to 3, 2 to 3, 3 to 3, 4 to 3, 5 to 3, 6 to 3, 7 to 3, 8 to 3,
                1 to 4, 2 to 4, 3 to 4, 4 to 4, 5 to 4, 6 to 4, 7 to 4, 8 to 4,
                1 to 5, 2 to 5, 3 to 5, 4 to 5, 5 to 5, 6 to 5, 7 to 5, 8 to 5,
                1 to 6, 2 to 6, 3 to 6, 4 to 6, 5 to 6, 6 to 6, 7 to 6, 8 to 6,
                2 to 7, 3 to 7, 4 to 7, 5 to 7, 6 to 7, 7 to 7,
                3 to 8, 4 to 8, 5 to 8, 6 to 8,
                4 to 9, 5 to 9
            )
            main.forEach { (x, y) -> pixel(x, y, baseColor) }
            // highlight: upper left arc
            val highlights = listOf(
                3 to 1, 4 to 1, 2 to 2, 1 to 3, 1 to 4, 1 to 5
            )
            highlights.forEach { (x, y) -> pixel(x, y, lightHighlight) }
            // pixel shine
            pixel(4, 2, highlightColor)
            // shadow: lower right arc
            val shadows = listOf(
                6 to 8, 7 to 7, 8 to 6, 8 to 5, 6 to 9, 5 to 9
            )
            shadows.forEach { (x, y) -> pixel(x, y, darkShadow) }
            // extra dark shadow for retro depth
            pixel(6, 8, darkShadowColor)
        }
        Figure.FigureType.TRIANGLE -> {
            // Taller isosceles triangle, higher and fills more of the grid
            val tri = listOf(
                5 to 2,
                4 to 3, 5 to 3, 6 to 3,
                3 to 4, 4 to 4, 5 to 4, 6 to 4, 7 to 4,
                2 to 5, 3 to 5, 4 to 5, 5 to 5, 6 to 5, 7 to 5, 8 to 5,
                1 to 6, 2 to 6, 3 to 6, 4 to 6, 5 to 6, 6 to 6, 7 to 6, 8 to 6, 9 to 6,
                1 to 7, 2 to 7, 3 to 7, 4 to 7, 5 to 7, 6 to 7, 7 to 7, 8 to 7, 9 to 7,
                0 to 8, 1 to 8, 2 to 8, 3 to 8, 4 to 8, 5 to 8, 6 to 8, 7 to 8, 8 to 8, 9 to 8, 10 to 8
            )
            tri.forEach { (x, y) -> pixel(x, y, baseColor) }
            // highlight: left/top edge of the triangle
            val highlights = listOf(
                5 to 2,
                4 to 3,
                3 to 4,
                2 to 5,
                1 to 6,
                1 to 7,
                0 to 8,
                1 to 8,
                2 to 8,
                3 to 8,
                4 to 8
            )
            highlights.forEach { (x, y) -> pixel(x, y, lightHighlight) }
            // pixel shine at the triangle peak
            pixel(5, 2, highlightColor)
            // shadow: right/bottom edge of the triangle
            val shadows = listOf(
                6 to 3,
                7 to 4,
                8 to 5,
                9 to 6,
                9 to 7,
                10 to 8,
                9 to 8,
                8 to 8,
                7 to 8,
                6 to 8
            )
            shadows.forEach { (x, y) -> pixel(x, y, darkShadow) }
            // extra dark shadow for retro
            pixel(10, 8, darkShadowColor)
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
