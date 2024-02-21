package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxot.seekandcatch.feature.gameplay.GameViewModel
import com.maxot.seekandcatch.feature.gameplay.R
import com.maxot.seekandcatch.feature.gameplay.data.getShapeForFigure

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
        viewModel.startGame(com.maxot.seekandcatch.feature.gameplay.data.GameMode.LevelsGameMode)
    }
    Box(modifier = Modifier
        .background(backgroundBrush)
        .fillMaxSize())

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
    goals: Set<com.maxot.seekandcatch.feature.gameplay.data.Goal<Any>>,
    figures: List<com.maxot.seekandcatch.feature.gameplay.data.Figure>,
    onFigureClick: (com.maxot.seekandcatch.feature.gameplay.data.Figure) -> Unit
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
        TaskLayout(goals = goals)

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
        ) {
            items(figures) { figure ->
                val shape: Shape = figure.getShapeForFigure()
                ColoredFigureLayout(color = figure.color, shape = shape) {
                    onFigureClick(figure)
                }
            }

        }
    }
}

@Composable
fun TaskLayout(goals: Set<com.maxot.seekandcatch.feature.gameplay.data.Goal<Any>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        val textStyle = TextStyle(Color.Black, fontSize = 30.sp)
        Text(text = stringResource(id = R.string.label_goal), style = textStyle)
        ListOfGoalsLayout(goals = goals)

    }
}

@Composable
fun ListOfGoalsLayout(goals: Set<com.maxot.seekandcatch.feature.gameplay.data.Goal<Any>>) {
    val textStyle = TextStyle(Color.Black, fontSize = 30.sp)
    goals.forEach { goal ->
        when (goal) {
            is com.maxot.seekandcatch.feature.gameplay.data.Goal.Colored -> {
                Text(text = stringResource(id = R.string.label_list_of_goal), style = textStyle)
                Box(
                    Modifier
                        .size(50.dp)
                        .padding(4.dp)
                        .background(goal.getGoal())
                )
                Text(text = "color", style = textStyle)
            }

            is com.maxot.seekandcatch.feature.gameplay.data.Goal.Figured -> {
                Text(text = stringResource(id = R.string.label_list_of_goal), style = textStyle)
                ColoredFigureLayout(
                    size = 50.dp,
                    color = goal.getGoal().color,
                    shape = goal.getGoal().getShapeForFigure()
                )
            }
        }
    }
}

@Composable
fun ColoredFigureLayout(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    color: Color?,
    shape: Shape,
    onItemClick: () -> Unit = {}
) {
    var visible by remember {
        mutableStateOf(true)
    }
    var colorState by remember {
        mutableStateOf(color)
    }
    var alreadyClicked by remember {
        mutableStateOf(false)
    }
    AnimatedVisibility(visible = visible, exit = fadeOut()) {
        Box(
            modifier = Modifier
                .then(modifier)
                .size(size)
                .padding(5.dp)
                .clip(shape)
                .background(colorState ?: Color.White)
                .border(width = 2.dp, color = Color.Black, shape = shape)
                .clickable {
                    if (!alreadyClicked) {
                        visible = false
                        alreadyClicked = true
                        onItemClick()
                        colorState = Color.White
                    }
                }

        )
    }
}

@Composable
fun NextLevelScreen(nextLevel: Int, goals: State<Set<com.maxot.seekandcatch.feature.gameplay.data.Goal<Any>>>) {
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
        ListOfGoalsLayout(goals = goals.value)
    }
}