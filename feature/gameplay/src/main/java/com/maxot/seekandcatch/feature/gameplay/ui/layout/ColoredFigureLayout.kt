package com.maxot.seekandcatch.feature.gameplay.ui.layout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import com.maxot.seekandcatch.core.designsystem.theme.LocalColorblindMode
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.designsystem.component.drawCircleFigure
import com.maxot.seekandcatch.core.designsystem.component.drawSquareFigure
import com.maxot.seekandcatch.core.designsystem.component.drawTriangleFigure
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.getShapeForFigure
import com.maxot.seekandcatch.feature.gameplay.R
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.random.Random

// Used for test
val AlphaKey = SemanticsPropertyKey<Float>("Alpha")
var SemanticsPropertyReceiver.alphaValue by AlphaKey

val ShapeKey = SemanticsPropertyKey<Shape>("Shape")
var SemanticsPropertyReceiver.shapeKey by ShapeKey

private data class Fragment(
    val id: Int,
    val initialOffset: Offset,
    val velocity: Offset,
    val rotationSpeed: Float,
    val size: Float,
    val type: Int, // 0: rect, 1: triangle, 2: circle (small shards)
    val color: Color
)

@Composable
fun ColoredFigureLayout(
    modifier: Modifier = Modifier,
    size: Dp? = null,
    figure: Figure,
    onItemClick: () -> Unit = {},
    isGameOver: Boolean = false,
    gameOverAnimDuration: Int = 800
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
        if (figure.isActive || isGameOver) 1f else 0f, label = "AlphaAnimation"
    )
    val alphaScore = remember { Animatable(1f) }

    val gameOverAlpha = remember { Animatable(1f) }
    val gameOverTranslation = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val gameOverRotation = remember { Animatable(0f) }
    val gameOverScale = remember { Animatable(1f) }

    val breakingProgress = remember(figure.id) { Animatable(0f) }
    val fragments = remember(figure.id, size) {
        val count = 20
        List(count) { i ->
            val angle = Random.nextFloat() * 2 * Math.PI
            val speed = 200f + Random.nextFloat() * 600f
            val vx = (Math.cos(angle) * speed).toFloat()
            val vy = (Math.sin(angle) * speed).toFloat() - 200f // Upward boost
            
            // Random initial position within a square roughly the size of the figure
            // We don't have exact size in Px here yet, but we can use a relative 0-1 range
            // and scale it later in Canvas. Or just assume a reasonable default.
            val initialX = (Random.nextFloat() - 0.5f) * 60f
            val initialY = (Random.nextFloat() - 0.5f) * 60f

            Fragment(
                id = i,
                initialOffset = Offset(initialX, initialY),
                velocity = Offset(vx, vy),
                rotationSpeed = (Random.nextFloat() * 1080 - 540),
                size = 5f + Random.nextFloat() * 15f,
                type = Random.nextInt(3),
                color = color
            )
        }
    }

    LaunchedEffect(figure.id, figure.pointsReceived) {
        if (figure.pointsReceived != null) {
            breakingProgress.animateTo(1f, tween(1000))
        } else {
            breakingProgress.snapTo(0f)
        }
    }

    LaunchedEffect(isGameOver) {
        if (isGameOver) {
            val jobs = listOf(
                launch {
                    gameOverAlpha.animateTo(0f, tween(gameOverAnimDuration))
                },
                launch {
                    val angle = Random.nextFloat() * 2 * Math.PI
                    val distance = 1000f
                    gameOverTranslation.animateTo(
                        Offset(
                            Math.cos(angle).toFloat() * distance,
                            Math.sin(angle).toFloat() * distance
                        ),
                        tween(gameOverAnimDuration)
                    )
                },
                launch {
                    gameOverRotation.animateTo(
                        (Random.nextFloat() * 720 - 360),
                        tween(gameOverAnimDuration)
                    )
                },
                launch {
                    gameOverScale.animateTo(0.2f, tween(gameOverAnimDuration))
                }
            )
            jobs.joinAll()
        }
    }

    Box(
        modifier = Modifier
            .semantics {
                contentDescription = coloredFigureContentDesc
                alphaValue = if (figure.pointsReceived != null || breakingProgress.value > 0f) 0f else alpha
                shapeKey = shape
            }
            .padding(10.dp)
            .run { size?.let { size(size) } ?: aspectRatio(1f) }
            .graphicsLayer {
                translationX = gameOverTranslation.value.x
                translationY = gameOverTranslation.value.y
                rotationZ = gameOverRotation.value
                scaleX = gameOverScale.value
                scaleY = gameOverScale.value
                this.alpha = if (isGameOver) gameOverAlpha.value else 1f
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = figure.isActive && !isGameOver && figure.pointsReceived == null
            ) {
                onItemClick()
            }
            .alpha(alpha)
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sizePx = size?.toPx() ?: this.size.minDimension
            if (breakingProgress.value == 0f) {
                when (figure.type) {
                    Figure.FigureType.SQUARE -> drawSquareFigure(sizePx, color)
                    Figure.FigureType.CIRCLE -> drawCircleFigure(sizePx, color)
                    Figure.FigureType.TRIANGLE -> drawTriangleFigure(sizePx, color)
                }
            } else if (breakingProgress.value < 1f) {
                fragments.forEach { fragment ->
                    val progress = breakingProgress.value
                    val time = progress * 1.0f // normalized time for physics
                    
                    // Simple physics: s = ut + 0.5at^2
                    // Gravity a = 1500 px/s^2 (downward)
                    val gravity = 2000f
                    val currentX = fragment.initialOffset.x + fragment.velocity.x * time
                    val currentY = fragment.initialOffset.y + fragment.velocity.y * time + 0.5f * gravity * time * time
                    
                    val currentRotation = fragment.rotationSpeed * time
                    val currentAlpha = (1f - progress * 1.2f).coerceIn(0f, 1f)

                    if (currentAlpha > 0) {
                        withTransform({
                            translate(currentX, currentY)
                            rotate(currentRotation, pivot = Offset(fragment.size / 2, fragment.size / 2))
                        }) {
                            drawFragment(fragment.type, fragment.size, fragment.color.copy(alpha = currentAlpha))
                        }
                    }
                }
            }
        }

        if (LocalColorblindMode.current) {
            val colorCode = when (figure.color) {
                Color.Red -> stringResource(id = com.maxot.seekandcatch.feature.settings.R.string.color_red)
                Color.Blue -> stringResource(id = com.maxot.seekandcatch.feature.settings.R.string.color_blue)
                Color.Green -> stringResource(id = com.maxot.seekandcatch.feature.settings.R.string.color_green)
                Color.Yellow -> stringResource(id = com.maxot.seekandcatch.feature.settings.R.string.color_yellow)
                else -> stringResource(id = com.maxot.seekandcatch.feature.settings.R.string.color_unknown)
            }
            Text(
                text = colorCode,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
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

private fun DrawScope.drawFragment(type: Int, fragmentSize: Float, color: Color) {
    when (type) {
        0 -> { // Square
            drawRect(color = color, size = Size(fragmentSize, fragmentSize))
        }

        2 -> { // Circle
            drawCircle(color = color, radius = fragmentSize / 2f)
        }

        1 -> { // Triangle
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(fragmentSize / 2, 0f)
                lineTo(fragmentSize, fragmentSize)
                lineTo(0f, fragmentSize)
                close()
            }
            drawPath(path = path, color = color)
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
