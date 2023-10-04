package com.maxot.seekandcatch.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxot.seekandcatch.data.Figure
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
                    ColorBox(color = figure.color) {
                        val condition = when (goal) {
                            is Goal.Colored -> {
                                goal.color == figure.color
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
fun ColorBox(color: Color, onItemClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(color)
            .clickable {
                onItemClick()
            }
    )
}
