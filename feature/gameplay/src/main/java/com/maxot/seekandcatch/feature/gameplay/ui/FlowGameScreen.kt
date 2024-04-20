package com.maxot.seekandcatch.feature.gameplay.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.feature.gameplay.FlowGameUiState
import com.maxot.seekandcatch.feature.gameplay.FlowGameViewModel
import com.maxot.seekandcatch.feature.gameplay.R
import com.maxot.seekandcatch.feature.gameplay.getDecimalPart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

const val TAG = "FlowGameScreen"

@Composable
fun FlowGameScreenRoute(
    viewModel: FlowGameViewModel = hiltViewModel(),
    toScoreScreen: () -> Unit
) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val score by viewModel.score.collectAsStateWithLifecycle()
    val figures by viewModel.figures.collectAsStateWithLifecycle()
    val coefficient by viewModel.coefficient.collectAsStateWithLifecycle()
    val flowGameUiState by viewModel.flowGameUiState.collectAsStateWithLifecycle()

    FlowGameScreen(
        goals = goals,
        score = score,
        figures = figures,
        coefficient = coefficient,
        flowGameUiState = flowGameUiState,
        onItemClick = { viewModel.onItemClick(it) },
        onItemsMissed = { start, end -> viewModel.onItemsMissed(start, end) },
        getAnimationDuration = { viewModel.getAnimationDuration() },
        toScoreScreen = toScoreScreen,
        resumeGame = { viewModel.resumeGame() },
        pauseGame = { viewModel.pauseGame() },
        finishGame = { viewModel.finishGame() }
    )
}

@Composable
fun FlowGameScreen(
    modifier: Modifier = Modifier,
    goals: Set<Goal<Any>>,
    score: Int,
    figures: List<Figure>,
    coefficient: Float,
    flowGameUiState: FlowGameUiState,
    onItemClick: (id: Int) -> Unit = {},
    onItemsMissed: (start: Int, end: Int) -> Unit = { i: Int, i1: Int -> },
    getAnimationDuration: () -> Int = { 10000 },
    toScoreScreen: () -> Unit = {},
    resumeGame: () -> Unit = {},
    pauseGame: () -> Unit = {},
    finishGame: () -> Unit = {}
) {

    val flowGameScreenContentDesc = stringResource(id = R.string.flow_game_screen_content_desc)
    val backgroundBrush =
        Brush.linearGradient(listOf(Color.Red, Color.Transparent, Color.Green, Color.Transparent))
    val beforeAnimationDelay = 1000L
    var scrollAnimationJob: Job = Job()

    val coroutineScope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    val showPauseDialog = remember { mutableStateOf(false) }

    val firstPassedItemPosition = remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex - 8 // Position of item that fully disappear from the viewport
        }
    }

    Box(
        modifier = Modifier
            .then(modifier)
            .semantics { contentDescription = flowGameScreenContentDesc }
            .background(backgroundBrush)
            .fillMaxSize()
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.label_score, score),
                style = MaterialTheme.typography.bodyLarge
            )
            GoalsLayout(goals = goals)

            CoefficientProgressLayout(
                progress = coefficient,
                currentCoefficient = coefficient.toInt()
            )

            GameFieldLayout(figures = figures, gridState = gridState) { id ->
                onItemClick(id)
            }
        }
        when (flowGameUiState) {
            FlowGameUiState.Active -> {
                LaunchedEffect(key1 = coefficient.toInt(), key2 = flowGameUiState) {
                    scrollAnimationJob = coroutineScope.launch {
//                        delay(beforeAnimationDelay)
                        gridState.animateScrollBy(
                            value = 100f * figures.size,
                            animationSpec = tween(
                                durationMillis = getAnimationDuration(),
                                easing = LinearEasing
                            )
                        )
                    }
                }
                /**
                 * Control over missed items.
                 * TODO: look for some different solution.
                 */
                LaunchedEffect(key1 = firstPassedItemPosition.value) {
                    if (firstPassedItemPosition.value >= 0) {
                        val startPosition = firstPassedItemPosition.value
                        Log.d(TAG, "firstPassedItemPosition ${firstPassedItemPosition.value}")
                        onItemsMissed(
                            startPosition,
                            startPosition + 3
                        )
                    }
                }
            }

            FlowGameUiState.Loading -> {

            }

            FlowGameUiState.Paused -> {
                showPauseDialog.value = true
                LaunchedEffect(key1 = Unit) {
                    gridState.stopScroll()
                }

                PauseDialog(
                    onDismissRequest = {
                        showPauseDialog.value = false
                        resumeGame()
                    },
                    onConfirmation = {
                        showPauseDialog.value = false
                        finishGame()
                    },
                    dialogTitle = stringResource(id = R.string.title_pause_dialog),
                    dialogText = stringResource(id = R.string.text_pause_dialog)
                )
            }

            FlowGameUiState.Finished -> {
                toScoreScreen()
            }
        }
    }

    BackHandler {
        pauseGame()
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_PAUSE) {
        if (flowGameUiState != FlowGameUiState.Finished)
            pauseGame()

    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        if (showPauseDialog.value)
            pauseGame() else resumeGame()
    }
}

@Composable
fun GameFieldLayout(
    modifier: Modifier = Modifier,
    figures: List<Figure>,
    gridState: LazyGridState,
    onItemClick: (id: Int) -> Unit
) {
    val gameFieldLayoutContentDesc = stringResource(id = R.string.game_field_layout_content_desc)
    //initial height set at 0.dp
    var gridHeight by remember { mutableStateOf(0.dp) }

    // get local density from composable
    val density = LocalDensity.current

    val gridCellsCount = 4

    LazyVerticalGrid(
        modifier = Modifier
            .then(modifier)
            .semantics { contentDescription = gameFieldLayoutContentDesc }
            .onGloballyPositioned {
                gridHeight = with(density) {
                    it.size.height.toDp()
                }

            },
        userScrollEnabled = false,
        state = gridState,
        columns = GridCells.Fixed(gridCellsCount),
    ) {
        for (i in 1..gridCellsCount) {
            item {
                //TODO: Need to change height to height of grid.
                Spacer(modifier = Modifier.height((LocalConfiguration.current.screenHeightDp - 150).dp))
            }
        }
        items(
            items = figures,
            key = { figure -> figure.id }
        ) { figure ->
            ColoredFigureLayout(
                figure = figure
            ) {
                onItemClick(figure.id)
            }
        }
    }

}

@Composable
fun CoefficientProgressLayout(
    modifier: Modifier = Modifier,
    progress: Float,
    currentCoefficient: Int
) {
    val coefficientProgressLayoutContentDesc =
        stringResource(id = R.string.coefficient_progress_layout_content_desc)
    Row(
        modifier = Modifier
            .then(modifier)
            .semantics { contentDescription = coefficientProgressLayoutContentDesc }
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "x${currentCoefficient}",
            style = MaterialTheme.typography.displaySmall
        )
        LinearProgressIndicator(progress = { progress.getDecimalPart() })
        Text(
            text = "x${(currentCoefficient + 1)}",
            style = MaterialTheme.typography.displayMedium
        )
    }
}


@Preview
@Composable
fun FlowGameScreenActivePreview() {
    val figures = listOf(
        Figure.getRandomFigure(1),
        Figure.getRandomFigure(2),
        Figure.getRandomFigure(3),
        Figure.getRandomFigure(4),
        Figure.getRandomFigure(5),
        Figure.getRandomFigure(6),
    )

    FlowGameScreen(
        goals = setOf(Goal.getRandomGoal()),
        score = 11,
        figures = figures,
        coefficient = 1f,
        flowGameUiState = FlowGameUiState.Active,
        toScoreScreen = { },
    )
}

@Preview
@Composable
fun FlowGameScreenPausedPreview() {
    val figures = listOf(
        Figure.getRandomFigure(1),
        Figure.getRandomFigure(2),
        Figure.getRandomFigure(3),
        Figure.getRandomFigure(4),
        Figure.getRandomFigure(5),
        Figure.getRandomFigure(6),
    )

    FlowGameScreen(
        goals = setOf(Goal.getRandomGoal()),
        score = 11,
        figures = figures,
        coefficient = 1f,
        flowGameUiState = FlowGameUiState.Paused,
    )
}