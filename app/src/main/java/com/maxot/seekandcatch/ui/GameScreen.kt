package com.maxot.seekandcatch.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.maxot.seekandcatch.MainActivityViewModel
import com.maxot.seekandcatch.R
import com.maxot.seekandcatch.data.GameMode
import com.maxot.seekandcatch.data.Goal
import com.maxot.seekandcatch.data.getShapeForFigure

@ExperimentalFoundationApi
@Composable
fun GameScreen(
    navController: NavController,
    viewModel: MainActivityViewModel
) {
    val goals = viewModel.goals.collectAsState()
    val score = viewModel.score.collectAsState()
    val figures = viewModel.figures.collectAsState()
    val isGameActive = viewModel.isGameActive.collectAsState()
    val isLevelChanged = viewModel.levelChanged.collectAsState()
    val level = viewModel.level.collectAsState()

    LaunchedEffect(viewModel.gameMode) {
        viewModel.startGame(GameMode.LevelsGameMode)
    }
    if (!isLevelChanged.value){
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
                    columns = GridCells.Adaptive(minSize = 80.dp),
                ) {
                    items(figures.value) { figure ->
                        val shape: Shape = figure.getShapeForFigure()
                        ColoredFigure(color = figure.color, shape = shape) {
                            viewModel.onFigureClick(figure)
                        }
                    }

                }
            }
        }
    } else {
        NextLevelScreen(nextLevel = level.value, goals = goals)
    }
}

@Composable
fun TaskView(goals: Set<Goal<Any>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        val textStyle = TextStyle(Color.Black, fontSize = 30.sp)
        Text(text = stringResource(id = R.string.label_goal), style = textStyle)
        ListOfGoalsView(goals = goals)

    }
}

@Composable
fun ListOfGoalsView(goals: Set<Goal<Any>>) {
    val textStyle = TextStyle(Color.Black, fontSize = 30.sp)
    goals.forEach { goal ->
        when (goal) {
            is Goal.Colored -> {
                Text(text = stringResource(id = R.string.label_list_of_goal), style = textStyle)
                Box(
                    Modifier
                        .size(50.dp)
                        .padding(4.dp)
                        .background(goal.getGoal())
                )
                Text(text = "color", style = textStyle)
            }
            is Goal.Figured -> {
                Text(text = stringResource(id = R.string.label_list_of_goal), style = textStyle)
                ColoredFigure(
                    size = 50.dp,
                    color = goal.getGoal().color,
                    shape = goal.getGoal().getShapeForFigure()
                )
            }
        }
    }
}

@Composable
fun ColoredFigure(size: Dp = 100.dp, color: Color?, shape: Shape, onItemClick: () -> Unit = {}) {
    var colorState by remember {
        mutableStateOf(color)
    }
    var alreadyClicked by remember {
        mutableStateOf(false)
    }
    Box(
        modifier = Modifier
            .size(size)
            .padding(5.dp)
            .clip(shape)
            .background(colorState ?: Color.White)
            .border(width = 2.dp, color = Color.Black, shape = shape)
            .clickable {
                if (!alreadyClicked) {
                    alreadyClicked = true
                    onItemClick()
                    colorState = Color.White
                }
            }
    )
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
        ListOfGoalsView(goals = goals.value)
    }
}