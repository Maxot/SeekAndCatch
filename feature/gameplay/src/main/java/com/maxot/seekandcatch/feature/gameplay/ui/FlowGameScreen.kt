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
import com.maxot.seekandcatch.feature.gameplay.getDecimalPart
import com.maxot.seekandcatch.core.domain.GameState
import com.maxot.seekandcatch.data.model.Figure
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@ExperimentalFoundationApi
@Composable
fun FlowGameScreen(
    viewModel: FlowGameViewModel = hiltViewModel(),
    toScoreScreen: () -> Unit
) {
    val backgroundBrush =
        Brush.linearGradient(listOf(Color.Red, Color.Transparent, Color.Green, Color.Transparent))
    val beforeAnimationDelay = 1000L

    val goals = viewModel.goals.collectAsState()
    val score = viewModel.score.collectAsState()
    val figures = viewModel.figures.collectAsState()
    val coefficient = viewModel.coefficient.collectAsState()
    val gameState = viewModel.gameState.collectAsState()
    var scrollAnimationJob: Job = Job()

    val coroutineScope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    val isPauseDialogShowed = remember { mutableStateOf(false) }

    /**
     * Start the game.
     */

    Box(
        modifier = Modifier
            .background(backgroundBrush)
            .fillMaxSize()
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.label_score, score.value),
                style = MaterialTheme.typography.bodyLarge
            )
            GoalsLayout(goals = goals.value)

            CoefficientProgressLayout(
                progress = coefficient.value,
                currentCoefficient = coefficient.value.toInt()
            )

            GameFieldLayout(figures = figures, gridState = gridState) { id ->
                viewModel.onItemClick(id)
            }
        }

        when (gameState.value) {
            GameState.CREATED -> {
                viewModel.startGame()
            }

            GameState.STARTED, GameState.RESUMED -> {
                /**
                 * Start scroll.
                 */
                LaunchedEffect(key1 = coefficient.value.toInt(), key2 = gameState.value) {
                    scrollAnimationJob = coroutineScope.launch {
//                        delay(beforeAnimationDelay)
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
                 * TODO: look for some different solution.
                 */
                LaunchedEffect(key1 = gridState) {
                    coroutineScope.launch {
                        snapshotFlow { gridState.firstVisibleItemIndex }
                            .collect {
                                //Position of item that fully disappear from the viewport
                                val start = gridState.firstVisibleItemIndex - 8
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
                        isPauseDialogShowed.value = false
                        viewModel.resumeGame()
                    },
                    onConfirmation = {
                        isPauseDialogShowed.value = false
                        viewModel.finishGame()
                    },
                    dialogTitle = stringResource(id = R.string.title_pause_dialog),
                    dialogText = stringResource(id = R.string.text_pause_dialog)
                )
            }

            GameState.FINISHED -> {
                viewModel.stopMusic()
                toScoreScreen()
            }
        }
    }

    BackHandler {
        viewModel.pauseGame()
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_PAUSE) {
        if (gameState.value != GameState.FINISHED)
            viewModel.pauseGame()
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        viewModel.resumeGame()
    }
}

@Composable
fun GameFieldLayout(
    figures: State<List<Figure>>,
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
                //TODO: Need to change height to height of grid.
                Spacer(modifier = Modifier.height((LocalConfiguration.current.screenHeightDp - 150).dp))
            }
        }
        items(
            items = figures.value,
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
        LinearProgressIndicator(progress = { progress.getDecimalPart() })
        Text(
            text = "x${(currentCoefficient + 1)}",
            style = MaterialTheme.typography.displayMedium
        )
    }
}
