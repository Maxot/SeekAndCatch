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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maxot.seekandcatch.core.designsystem.icon.SaCIcons
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.feature.gameplay.FlowGameUiState
import com.maxot.seekandcatch.feature.gameplay.FlowGameViewModel
import com.maxot.seekandcatch.feature.gameplay.R
import com.maxot.seekandcatch.feature.gameplay.ui.layout.CoefficientProgressLayout
import com.maxot.seekandcatch.feature.gameplay.ui.layout.ColoredFigureLayout
import com.maxot.seekandcatch.feature.gameplay.ui.layout.GoalsLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val TAG = "FlowGameScreen"

@Composable
fun FlowGameScreenRoute(
    viewModel: FlowGameViewModel = hiltViewModel(),
    toGameResultScreen: () -> Unit
) {
    val rowWidth = viewModel.getRowWidth()
    val lifeCount by viewModel.lifeCount.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val score by viewModel.score.collectAsStateWithLifecycle()
    val figures by viewModel.figures.collectAsStateWithLifecycle()
    val coefficient by viewModel.coefficient.collectAsStateWithLifecycle()
    val gameDuration by viewModel.gameDuration.collectAsStateWithLifecycle()
    val flowGameUiState by viewModel.flowGameUiState.collectAsStateWithLifecycle()

    FlowGameScreen(
        rowWidth = rowWidth,
        lifeCount = lifeCount,
        goals = goals,
        score = score,
        figures = figures,
        coefficient = coefficient,
        gameDuration = gameDuration,
        flowGameUiState = flowGameUiState,
        onItemClick = viewModel::onItemClick,
        toGameResultScreen = toGameResultScreen,
        resumeGame = viewModel::resumeGame,
        pauseGame = viewModel::pauseGame,
        finishGame = viewModel::finishGame,
        onItemHeightMeasured = viewModel::setItemHeight,
        getScrollDuration = viewModel::getScrollDuration,
        getPixelsToScroll = viewModel::getPixelsToScroll,
        onFirstVisibleItemIndexChanged = viewModel::onFirstVisibleItemIndexChanged
    )
}

@Composable
fun FlowGameScreen(
    modifier: Modifier = Modifier,
    rowWidth: Int = 4,
    lifeCount: Int = 3,
    goals: Set<Goal<Any>>,
    score: Int,
    figures: List<Figure>,
    coefficient: Float,
    gameDuration: Long,
    flowGameUiState: FlowGameUiState,
    onItemClick: (id: Int) -> Unit = {},
    toGameResultScreen: () -> Unit = {},
    resumeGame: () -> Unit = {},
    pauseGame: () -> Unit = {},
    finishGame: () -> Unit = {},
    onItemHeightMeasured: (height: Int) -> Unit = { },
    getScrollDuration: () -> Int = { 10000 },
    getPixelsToScroll: () -> Float,
    onFirstVisibleItemIndexChanged: (index: Int) -> Unit = {}
) {
    val density = LocalDensity.current

    val flowGameScreenContentDesc = stringResource(id = R.string.flow_game_screen_content_desc)

    val backgroundBrush =
        Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.primary,
                Color.Transparent,
                MaterialTheme.colorScheme.secondary,
                Color.Transparent
            )
        )

    val beforeAnimationDelay = 1000L

    val coroutineScope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    val showPauseDialog = remember { mutableStateOf(false) }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    var gameInfoPanelSize by remember {
        mutableStateOf(0.dp)
    }
    val spacerHeight by remember {
        derivedStateOf {
            screenHeight - gameInfoPanelSize
        }
    }

    LaunchedEffect(key1 = true) {
        snapshotFlow { gridState.firstVisibleItemIndex }
            .collect {
                Log.d(TAG, "firstVisibleItemIndex: ${gridState.firstVisibleItemIndex}")
                onFirstVisibleItemIndexChanged(gridState.firstVisibleItemIndex)
            }
    }

    Box(
        modifier = Modifier
            .background(backgroundBrush)
            .then(modifier)
            .semantics { contentDescription = flowGameScreenContentDesc }
            .fillMaxSize()
    ) {
        Column {
            GameInfoPanel(
                modifier = Modifier
                    .onGloballyPositioned {
                        gameInfoPanelSize = with(density) {
                            it.size.height.toDp() // Height of GameInfoPanel
                        }
                    },
                lifeCount = lifeCount,
                goals = goals,
                score = score,
                coefficient = coefficient,
                gameDuration = gameDuration
            )

            GameFieldLayout(
                gridWidth = rowWidth,
                spacerHeight = spacerHeight,
                figures = figures,
                gridState = gridState,
                onItemHeightMeasured = onItemHeightMeasured,
                onItemClick = onItemClick
            )
        }

        when (flowGameUiState) {
            FlowGameUiState.Active -> {
                LaunchedEffect(
                    key1 = coefficient.toInt(),
                    key2 = figures.size,
                    key3 = (gameDuration / 1000 / 30), // each 30 second update
                ) {
                    val pixelsToScrollWithSpacers =
                        getPixelsToScroll() + with(density) {
                            spacerHeight.toPx()
                        }
                    gridState.animateScrollBy(
                        value = pixelsToScrollWithSpacers,
                        animationSpec = tween(
                            durationMillis = getScrollDuration(),
                            easing = LinearEasing
                        )
                    )
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
                toGameResultScreen()
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
fun GameInfoPanel(
    modifier: Modifier = Modifier,
    lifeCount: Int = 3,
    goals: Set<Goal<Any>>,
    score: Int,
    coefficient: Float,
    gameDuration: Long,
) {
    val maxLifeCount = 5
    Column(
        modifier = Modifier
            .then(modifier)
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .width(100.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = stringResource(id = R.string.feature_gameplay_label_score, score),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Time: ${formatMilliseconds(gameDuration)}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    repeat(maxLifeCount - lifeCount) {
                        Icon(imageVector = SaCIcons.UnselectedFavorite, contentDescription = "")
                    }
                    repeat(lifeCount) {
                        Icon(imageVector = SaCIcons.Favorite, contentDescription = "")
                    }
                }

                GoalsLayout(goals = goals)
            }

        }

        CoefficientProgressLayout(
            progress = coefficient,
            currentCoefficient = coefficient.toInt()
        )
    }
}

@Composable
fun GameFieldLayout(
    modifier: Modifier = Modifier,
    gridWidth: Int = 4,
//    itemsSize: Dp = 100.dp,
    spacerHeight: Dp,
    figures: List<Figure>,
    gridState: LazyGridState,
    onItemHeightMeasured: (height: Int) -> Unit = { },
    onItemClick: (id: Int) -> Unit
) {
    val gameFieldLayoutContentDesc = stringResource(id = R.string.game_field_layout_content_desc)

    LazyVerticalGrid(
        modifier = Modifier
            .then(modifier)
            .semantics { contentDescription = gameFieldLayoutContentDesc },
        userScrollEnabled = false,
        state = gridState,
        columns = GridCells.Fixed(gridWidth),
    ) {
        // Add spacer for one row to reach scrolling from empty space
        repeat(gridWidth) {
            item {
                Spacer(
                    modifier = Modifier
                        .height(spacerHeight)
                )
            }
        }
        items(
            items = figures,
            key = { figure -> figure.id }
        ) { figure ->
            ColoredFigureLayout(
                modifier = Modifier
                    .onGloballyPositioned {
                        onItemHeightMeasured(it.size.height)
                    },
                figure = figure
            ) {
                onItemClick(figure.id)
            }
        }
        // Add spacer for one row to reach scrolling to empty space
        repeat(gridWidth) {
            item {
                Spacer(
                    modifier = Modifier
                        .height(spacerHeight)
                )
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
        gameDuration = 1000,
        flowGameUiState = FlowGameUiState.Active,
        toGameResultScreen = { },
        getPixelsToScroll = { 1000f },
        onFirstVisibleItemIndexChanged = {}
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
        gameDuration = 1000,
        flowGameUiState = FlowGameUiState.Paused,
        getPixelsToScroll = { 1000f },
        onFirstVisibleItemIndexChanged = {}
    )
}

fun formatMilliseconds(milliseconds: Long): String {
    val format = SimpleDateFormat("mm:ss", Locale.US)
    return format.format(Date(milliseconds))
}
