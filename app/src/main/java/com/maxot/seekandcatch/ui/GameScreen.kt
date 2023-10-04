package com.maxot.seekandcatch.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxot.seekandcatch.data.Figure
import com.maxot.seekandcatch.data.FigureType
import com.maxot.seekandcatch.data.Goal

@ExperimentalFoundationApi
@Composable
fun GameScreen(
    goal: Goal,
    figures: State<List<Figure>>,
    onStopGame: (Int) -> Unit
) {
    var score by remember {
        mutableStateOf(0)
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column {
            Text(
                text = "Score: $score", modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                color = Color.Black,
                textAlign = TextAlign.Center,
                fontSize = 30.sp
            )
            Text(
                text = "Goal: ${goal.getString()}", modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                color = Color.Black,
                textAlign = TextAlign.Center,
                fontSize = 30.sp
            )
            LazyVerticalGrid(
                cells = GridCells.Adaptive(minSize = 80.dp)
            ) {
                items(figures.value) { figure ->
                    val shape: Shape  = figure.getShapeForFigure()

                    ColoredFigure(color = figure.color, shape = shape) {
                        val condition = when (goal) {
                            is Goal.Colored -> {
                                goal.color == figure.color
                            }
                            is Goal.Figured -> {
                                false
                            }
                        }
                        if (condition) {
                            score += 1
                            figure.color = Color.White
                        } else {
                            onStopGame(score)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColoredFigure(color: Color, shape: Shape, onItemClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(shape)
            .background(color)
            .border(border = BorderStroke(2.dp, Color.Black))
            .clickable {
                onItemClick()
            }
    )
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

