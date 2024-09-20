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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maxot.seekandcatch.core.designsystem.icon.SaCIcons
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.GameMode
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.feature.gameplay.FlowGameUiState
import com.maxot.seekandcatch.feature.gameplay.FlowGameViewModel
import com.maxot.seekandcatch.feature.gameplay.R
import com.maxot.seekandcatch.feature.gameplay.model.FlowGameUiEvent
import com.maxot.seekandcatch.feature.gameplay.ui.layout.CoefficientProgressLayout
import com.maxot.seekandcatch.feature.gameplay.ui.layout.ColoredFigureLayout
import com.maxot.seekandcatch.feature.gameplay.ui.layout.DetailedGoalsLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val TAG = "FlowGameScreen"

@Composable
fun FlowGameScreenRoute(
    viewModel: FlowGameViewModel = hiltViewModel(),
    toGameResultScreen: () -> Unit
) {
    val gridState: LazyGridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    val gameMode = viewModel.selectedGameMode.collectAsStateWithLifecycle()
    val flowGameUiState by viewModel.flowGameUiState.collectAsStateWithLifecycle()

    val showPauseDialog = remember { mutableStateOf(false) }

    FlowGameScreen(
        gameMode = gameMode.value,
        gridState = gridState,
        flowGameUiState = flowGameUiState,
        sendEvent = { flowGameUiEvent -> viewModel.onEvent(flowGameUiEvent) },
        toGameResultScreen = toGameResultScreen,
        showPauseDialog = showPauseDialog.value,
        updatePauseDialogVisibility = { showPauseDialog.value = it }
    )

    LaunchedEffect(key1 = true) {
        snapshotFlow { gridState.firstVisibleItemIndex }
            .collect {
                Log.d(TAG, "firstVisibleItemIndex: ${gridState.firstVisibleItemIndex}")
                viewModel.onEvent(FlowGameUiEvent.FirstVisibleItemIndexChanged(gridState.firstVisibleItemIndex))
            }
    }

    if (flowGameUiState.isReady && !flowGameUiState.isActive) {
        ReadyToGameLayout(
            goals = flowGameUiState.goals,
            goalsSuitableFigures = flowGameUiState.goalSuitableFigures,
            setGameReadyToStart = { viewModel.onEvent(FlowGameUiEvent.SetGameReadyToStart) }
        )
    }

    if (flowGameUiState.isPaused) {
        showPauseDialog.value = true
        LaunchedEffect(key1 = Unit) {
            gridState.stopScroll()
        }

        PauseDialog(
            onDismissRequest = {
                viewModel.onEvent(FlowGameUiEvent.ResumeGame)
                showPauseDialog.value = false
            },
            onConfirmation = {
                showPauseDialog.value = false
                viewModel.onEvent(FlowGameUiEvent.FinishGame)
            },
            dialogTitle = stringResource(id = R.string.title_pause_dialog),
            dialogText = stringResource(id = R.string.text_pause_dialog)
        )
    }

    if (flowGameUiState.isFinished) {
        LaunchedEffect(key1 = true) {
            coroutineScope.launch {
                delay(1)
                toGameResultScreen()
            }
        }
    }

    BackHandler {
        viewModel.onEvent(FlowGameUiEvent.PauseGame)
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_PAUSE) {
        if (!flowGameUiState.isFinished)
            viewModel.onEvent(FlowGameUiEvent.PauseGame)
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        val event =
            if (showPauseDialog.value) FlowGameUiEvent.PauseGame else FlowGameUiEvent.ResumeGame
        viewModel.onEvent(event)
    }
}

@Composable
fun FlowGameScreen(
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    gameMode: GameMode = GameMode.FLOW,
    flowGameUiState: FlowGameUiState,
    sendEvent: (FlowGameUiEvent) -> Unit,
    toGameResultScreen: () -> Unit = {},
    showPauseDialog: Boolean = false,
    updatePauseDialogVisibility: (Boolean) -> Unit = {}
) {
    val density = LocalDensity.current
    val flowGameScreenContentDesc = stringResource(id = R.string.flow_game_screen_content_desc)

    val largeRadialGradient = object : ShaderBrush() {
        override fun createShader(size: Size): Shader {
            val biggerDimension = maxOf(size.height, size.width)
            return RadialGradientShader(
                colors = listOf(Color.Transparent, Color.Red.copy(alpha = 0.4f)),
                center = size.center,
                radius = biggerDimension / 2f,
                colorStops = listOf(0f, 0.95f)
            )
        }
    }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    var gameInfoPanelSize by remember {
        mutableStateOf(0.dp)
    }
    val spacerHeight by remember {
        derivedStateOf {
            screenHeight - gameInfoPanelSize
        }
    }

    Box(
        modifier = Modifier
            .semantics { contentDescription = flowGameScreenContentDesc }
            .fillMaxSize()
            .then(modifier)
    ) {

        Column {
            GameInfoPanel(
                modifier = Modifier
                    .onGloballyPositioned {
                        gameInfoPanelSize = with(density) {
                            it.size.height.toDp() // Height of GameInfoPanel
                        }
                    },
                maxLifeCount = flowGameUiState.maxLifeCount,
                lifeCount = flowGameUiState.lifeCount,
                goals = flowGameUiState.goals,
                goalsSuitableFigures = flowGameUiState.goalSuitableFigures,
                score = flowGameUiState.score,
                coefficient = flowGameUiState.coefficient,
                gameDuration = flowGameUiState.gameDuration
            )

            GameFieldLayout(
                gridWidth = flowGameUiState.rowWidth,
                spacerHeight = spacerHeight,
                figures = flowGameUiState.figures,
                gridState = gridState,
                onItemHeightMeasured = { height ->
                    sendEvent(
                        FlowGameUiEvent.ItemHeightMeasured(
                            height
                        )
                    )
                },
                onItemClick = { id -> sendEvent(FlowGameUiEvent.OnItemClick(id)) },
                reverseLayout = gameMode != GameMode.FLOW
            )
        }

        /**
         * Action responsible for scrolling list of items.
         */
        LaunchedEffect(
            key1 = flowGameUiState.scrollDuration,
            key2 = flowGameUiState.pixelsToScroll,
            key3 = flowGameUiState.isActive
        ) {
            if (flowGameUiState.isActive && flowGameUiState.scrollDuration > 0 && flowGameUiState.pixelsToScroll > 0) {
                val pixelsToScrollWithSpacers =
                    flowGameUiState.pixelsToScroll + with(density) {
                        spacerHeight.toPx()
                    }
                gridState.animateScrollBy(
                    value = pixelsToScrollWithSpacers,
                    animationSpec = tween(
                        durationMillis = flowGameUiState.scrollDuration,
                        easing = LinearEasing
                    )
                )
            }
        }
    }

    val wastedBackground = if (flowGameUiState.isLifeWasted)
        largeRadialGradient else Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color.Transparent
        )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(wastedBackground)
    )
}

@Composable
fun ReadyToGameLayout(
    modifier: Modifier = Modifier,
    goals: Set<Goal<Any>>,
    goalsSuitableFigures: Set<Figure>,
    setGameReadyToStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var countDown by remember {
            mutableStateOf(3)
        }

        val text = if (countDown > 0) "$countDown" else "Go!"

//        GoalsLayout(
//            modifier = Modifier,
//            goals = goals,
//            textStyle = MaterialTheme.typography.displaySmall
//        )
        DetailedGoalsLayout(goalsSuitableFigures = goalsSuitableFigures)
        Text(
            text = stringResource(R.string.feature_gameplay_click_on_items),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )

        LaunchedEffect(key1 = Unit) {
            repeat(3) {
                delay(1_000)
                countDown--
            }
            delay(500)
            setGameReadyToStart()
        }

        Text(
            text = text,
            style = MaterialTheme.typography.displayLarge
        )

    }
}

@Composable
fun GameInfoPanel(
    modifier: Modifier = Modifier,
    maxLifeCount: Int = 5,
    lifeCount: Int = 3,
    goals: Set<Goal<Any>>,
    goalsSuitableFigures: Set<Figure>,
    score: Int,
    coefficient: Float,
    gameDuration: Long,
) {
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

//                GoalsLayout(goals = goals)
                DetailedGoalsLayout(goalsSuitableFigures = goalsSuitableFigures)
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
    onItemClick: (id: Int) -> Unit,
    reverseLayout: Boolean = false
) {
    val gameFieldLayoutContentDesc = stringResource(id = R.string.game_field_layout_content_desc)

    LazyVerticalGrid(
        modifier = Modifier
            .then(modifier)
            .semantics { contentDescription = gameFieldLayoutContentDesc },
        userScrollEnabled = false,
        state = gridState,
        columns = GridCells.Fixed(gridWidth),
        reverseLayout = reverseLayout
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
                figure = figure,
                onItemClick = { onItemClick(figure.id) },
            )
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

/**
 * Previews
 */

@Preview
@Composable
fun FlowGameScreenLoadingPreview() {
    val figures = listOf(
        Figure.getRandomFigure(1),
        Figure.getRandomFigure(2),
        Figure.getRandomFigure(3),
        Figure.getRandomFigure(4),
        Figure.getRandomFigure(5),
        Figure.getRandomFigure(6),
    )

    FlowGameScreen(
        flowGameUiState = FlowGameUiState(figures = figures, isLoading = true),
        sendEvent = { },
        toGameResultScreen = { },
    )
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
        flowGameUiState = FlowGameUiState(figures = figures, isActive = true),
        sendEvent = { },
        toGameResultScreen = { },
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
        flowGameUiState = FlowGameUiState(isPaused = true, figures = figures),
        sendEvent = { },
    )
}

fun formatMilliseconds(milliseconds: Long): String {
    val format = SimpleDateFormat("mm:ss", Locale.US)
    return format.format(Date(milliseconds))
}
