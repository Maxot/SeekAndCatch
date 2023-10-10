package com.maxot.seekandcatch.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxot.seekandcatch.MainActivityViewModel
import com.maxot.seekandcatch.R
import com.maxot.seekandcatch.data.GameMode
import com.maxot.seekandcatch.data.getShapeForFigure
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@Composable
fun FlowModeGameScreen(
    viewModel: MainActivityViewModel
) {
    val goals = viewModel.goals.collectAsState()
    val score = viewModel.score.collectAsState()
    val figures = viewModel.figures.collectAsState()

    LaunchedEffect(viewModel.gameMode) {
        viewModel.startGame(GameMode.FlowGameMode)
    }

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.label_score, score.value), modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                color = Color.Black,
                textAlign = TextAlign.Center,
                fontSize = 30.sp
            )
            TaskView(goals = goals.value)

            LazyVerticalGrid(
                userScrollEnabled = false,
                state = gridState,
                columns = GridCells.Adaptive(minSize = 80.dp),
            ) {
                items(figures.value) { figure ->
                    val shape: Shape = figure.getShapeForFigure()
                    ColoredFigure(color = figure.color, shape = shape) {
                        viewModel.onFigureClick(figure)
                    }
                }
                coroutineScope.launch {
                    gridState.animateScrollBy(
                        value = 8000f,
                        animationSpec = tween(durationMillis = 15000, easing = LinearEasing)
                    )
                }

            }
        }
    }
}
