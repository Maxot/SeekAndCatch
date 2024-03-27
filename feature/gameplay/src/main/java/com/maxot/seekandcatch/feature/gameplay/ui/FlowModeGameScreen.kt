package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxot.seekandcatch.feature.gameplay.GameViewModel
import com.maxot.seekandcatch.feature.gameplay.R
import com.maxot.seekandcatch.feature.gameplay.data.GameMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Deprecated("")
@ExperimentalFoundationApi
@Composable
fun FlowModeGameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    toScoreScreen: () -> Unit
) {
    val backgroundBrush =
        Brush.linearGradient(listOf(Color.Red, Color.Transparent, Color.Green, Color.Transparent))

    val beforeAnimationDelay = 1000L
    val levelDuration = viewModel.getLevelDuration()

    val goals = viewModel.goals.collectAsState()
    val score = viewModel.score.collectAsState()
    val figures = viewModel.figures.collectAsState()
    val uiState = viewModel.gameUiState.collectAsState()
    val level = viewModel.level.collectAsState()
    val timeToEnd = viewModel.timeToEnd.collectAsState()

    var scrollAnimationJob: Job = Job()

    LaunchedEffect(viewModel.gameMode) {
        viewModel.startGame(GameMode.FlowGameMode)
    }

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .background(backgroundBrush)
            .fillMaxSize()
    ) {
        when (uiState.value) {
            GameViewModel.GameUiState.GameEnded -> {
                toScoreScreen()
            }

            GameViewModel.GameUiState.InProgress -> {
                Column {
                    Text(
                        text = stringResource(id = R.string.label_score, score.value),
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth(),
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontSize = 30.sp
                    )
//                    Text(
//                        text = "Time to end: ${timeToEnd.value} ms",
//                        modifier = Modifier
//                            .height(50.dp)
//                            .fillMaxWidth(),
//                        color = Color.Black,
//                        textAlign = TextAlign.Center,
//                        fontSize = 30.sp
//                    )

                    GoalsLayout(goals = goals.value)

                    LazyVerticalGrid(
                        userScrollEnabled = false,
                        state = gridState,
                        columns = GridCells.Fixed(4),
                    ) {
                        items(figures.value, contentType = { it.type }) { figure ->
                            ColoredFigureLayout(
                                figure = figure
                            ) {
                                viewModel.onFigureClick(figure)
                            }
                        }
                    }
                    LaunchedEffect(key1 = level.value) {
                        scrollAnimationJob.cancel()
                        gridState.stopScroll()
                        gridState.scrollToItem(0)
                        scrollAnimationJob = coroutineScope.launch {
                            delay(beforeAnimationDelay)
                            gridState.animateScrollBy(
                                value = 100f * figures.value.size,
                                animationSpec = tween(
                                    durationMillis = levelDuration.toInt(),
                                    easing = LinearEasing
                                )
                            )
                        }
                    }
                }
            }

            GameViewModel.GameUiState.LevelPreview -> {
                NextLevelScreen(
                    nextLevel = level.value,
                    goals = goals
                )
            }

        }
    }


}
