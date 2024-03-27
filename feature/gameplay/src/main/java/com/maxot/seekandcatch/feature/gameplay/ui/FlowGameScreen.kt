package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.maxot.seekandcatch.feature.gameplay.FlowGameViewModel
import com.maxot.seekandcatch.feature.gameplay.R
import com.maxot.seekandcatch.feature.gameplay.data.Figure
import com.maxot.seekandcatch.feature.gameplay.removeIntegerPart
import com.maxot.seekandcatch.feature.gameplay.usecase.GameState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@ExperimentalFoundationApi
@Composable
fun FlowGameScreen(
    viewModel: FlowGameViewModel = hiltViewModel(),
    toScoreScreen: () -> Unit
) {
    val beforeAnimationDelay = 1000L

    val goals = viewModel.goals.collectAsState()
    val score = viewModel.score.collectAsState()
    val figures = viewModel.figures.collectAsState()
    val coefficient = viewModel.coefficient.collectAsState()
    val coefficientProgress = viewModel.coefficientProgress.collectAsState()
    val gameState = viewModel.gameState.collectAsState()
    var scrollAnimationJob: Job = Job()

    val coroutineScope = rememberCoroutineScope()

    val backgroundBrush =
        Brush.linearGradient(listOf(Color.Red, Color.Transparent, Color.Green, Color.Transparent))


    val gridState = rememberLazyGridState()
    val pauseDialog = remember { mutableStateOf(false) }
    /**
     * Start the game.
     */
    LaunchedEffect(Unit) {
        viewModel.startGame()
    }

    Box(
        modifier = Modifier
            .background(backgroundBrush)
            .fillMaxSize()
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.label_score, score.value),
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge
            )
            GoalsLayout(goals = goals.value)

            CoefficientProgressLayout(coefficientProgress.value, coefficient.value)

            GameFieldLayout(figures = figures, gridState = gridState) { id ->
                viewModel.onItemClick(id)
            }
        }

        when (gameState.value) {
            GameState.FINISHED -> {
                toScoreScreen()
            }

            GameState.RESUMED -> {
                /**
                 * Start scroll.
                 */
                LaunchedEffect(key1 = coefficient.value, key2 = gameState.value) {
                    scrollAnimationJob = coroutineScope.launch {
                        delay(beforeAnimationDelay)
                        gridState.animateScrollBy(
                            value = 100f * figures.value.size,
                            animationSpec = tween(
                                durationMillis = viewModel.getAnimationDuration(),
                                easing = LinearEasing
                            )
                        )
                    }
                }
                /**
                 * Control over missed items.
                 */
                LaunchedEffect(key1 = gridState) {
                    coroutineScope.launch {
                        snapshotFlow { gridState.firstVisibleItemIndex }
                            .collect {
                                val start = gridState.firstVisibleItemIndex - 4
                                if (start >= 0)
                                    viewModel.onItemsMissed(start, start + 3)
                            }
                    }
                }
            }

            GameState.PAUSED -> {
                LaunchedEffect(key1 = Unit) {
                    gridState.stopScroll()
                }

                PauseDialog(
                    onDismissRequest = {
                        pauseDialog.value = false
                        viewModel.resumeGame()
                    },
                    onConfirmation = {
                        pauseDialog.value = false
                        viewModel.resumeGame()
                        toScoreScreen()
                    },
                    dialogTitle = "Pause",
                    dialogText = "Do you really want to exit the game?"
                )
            }

            GameState.STARTED -> {

            }
        }
    }

    BackHandler {
        viewModel.pauseGame()
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_PAUSE) {
        coroutineScope.launch {
            gridState.stopScroll()
        }
    }
    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        coroutineScope.launch {
            scrollAnimationJob = coroutineScope.launch {
                delay(beforeAnimationDelay)
                gridState.animateScrollBy(
                    value = 100f * figures.value.size,
                    animationSpec = tween(
                        durationMillis = viewModel.getAnimationDuration(),
                        easing = LinearEasing
                    )
                )
            }
        }
    }
}

@Composable
fun GameFieldLayout(
    figures: State<Map<Int, Figure>>,
    gridState: LazyGridState,
    onItemClick: (id: Int) -> Unit
) {
    //initial height set at 0.dp
    var gridHeight by remember { mutableStateOf(0.dp) }

    // get local density from composable
    val density = LocalDensity.current

    val gridCellsCount = 4

    LazyVerticalGrid(
        modifier = Modifier.onGloballyPositioned {
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
                Spacer(modifier = Modifier.height((LocalConfiguration.current.screenHeightDp - 150).dp))
//                    Spacer(modifier = Modifier.height(gridHeight))
            }
        }
        items(
            figures.value.values.toList(),
//                            contentType = { it.type },
            key = { figure -> figure.id }) { figure ->
            ColoredFigureLayout(
                figure = figure
            ) {
                onItemClick(figure.id)
            }
        }
    }

}

@Composable
fun CoefficientProgressLayout(progress: Float, currentCoefficient: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "x${currentCoefficient}",
            style = MaterialTheme.typography.displaySmall
        )
        LinearProgressIndicator(progress = { progress.removeIntegerPart() })
        Text(
            text = "x${(currentCoefficient + 1)}",
            style = MaterialTheme.typography.displayMedium
        )
    }
}
