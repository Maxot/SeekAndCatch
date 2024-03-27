package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxot.seekandcatch.feature.gameplay.GameViewModel
import com.maxot.seekandcatch.feature.gameplay.R
import com.maxot.seekandcatch.feature.gameplay.data.Figure
import com.maxot.seekandcatch.feature.gameplay.data.GameMode
import com.maxot.seekandcatch.feature.gameplay.data.Goal

@Deprecated("")
@ExperimentalFoundationApi
@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    toScoreScreen: () -> Unit
) {
    val backgroundBrush =
        Brush.linearGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Blue))

    val gameUiState = viewModel.gameUiState.collectAsState()
    val goals = viewModel.goals.collectAsState()
    val score = viewModel.score.collectAsState()
    val figures = viewModel.figures.collectAsState()
    val level = viewModel.level.collectAsState()

    LaunchedEffect(viewModel.gameMode) {
        viewModel.startGame(GameMode.LevelsGameMode)
    }
    Box(
        modifier = Modifier
            .background(backgroundBrush)
            .fillMaxSize()
    )

    when (gameUiState.value) {
        is GameViewModel.GameUiState.GameEnded -> {
            toScoreScreen()
        }

        is GameViewModel.GameUiState.InProgress -> {
            FiguresLayout(
                score = score.value,
                goals = goals.value,
                figures = figures.value
            ) { figure ->
                viewModel.onFigureClick(figure)
            }
        }

        is GameViewModel.GameUiState.LevelPreview -> {
            NextLevelScreen(nextLevel = level.value, goals = goals)
        }
    }
}

@Composable
fun FiguresLayout(
    modifier: Modifier = Modifier,
    score: Int,
    goals: Set<Goal<Any>>,
    figures: List<Figure>,
    onFigureClick: (Figure) -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.label_score, score), modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(),
            color = Color.Black,
            textAlign = TextAlign.Center,
            fontSize = 30.sp
        )
        GoalsLayout(goals = goals)

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
        ) {
            items(figures, contentType = { it.type }) { figure ->
                ColoredFigureLayout(figure = figure) {
                    onFigureClick(figure)
                }
            }

        }
    }
}

@Composable
fun NextLevelScreen(nextLevel: Int, goals: State<Set<Goal<Any>>>) {
    val textStyle = TextStyle(Color.Black, fontSize = 30.sp)
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(id = R.string.label_next_level, nextLevel), style = textStyle)
        Spacer(modifier = Modifier.height(5.dp))
        Text(text = stringResource(id = R.string.label_next_level_goal), style = textStyle)
        Spacer(modifier = Modifier.height(5.dp))
        GoalsLayout(goals = goals.value)
    }
}