package com.maxot.seekandcatch.feature.gameplay.ui.flowgame

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maxot.seekandcatch.core.common.model.GameMode
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.feature.gameplay.flashRed
import com.maxot.seekandcatch.feature.gameplay.model.FlowGameUiEvent
import com.maxot.seekandcatch.feature.gameplay.moveAndScale
import com.maxot.seekandcatch.feature.gameplay.shake
import com.maxot.seekandcatch.feature.gameplay.ui.PauseDialog
import com.maxot.seekandcatch.feature.gameplay.ui.flowgame.model.FlowGameUiState
import com.maxot.seekandcatch.feature.gameplay.ui.layout.DetailedGoalsLayout
import com.maxot.seekandcatch.feature.gameplay.ui.layout.FlowGameFieldLayout
import com.maxot.seekandcatch.feature.gameplay.ui.layout.GameInfoPanel
import kotlinx.coroutines.delay
import com.maxot.seekandcatch.feature.gameplay.generated.resources.Res
import com.maxot.seekandcatch.feature.gameplay.generated.resources.*
import com.maxot.seekandcatch.core.designsystem.generated.resources.Res as DesignRes
import com.maxot.seekandcatch.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

const val TAG = "FlowGameScreen"

@Composable
fun FlowGameScreen(
    viewModel: FlowGameViewModel = koinViewModel(),
    toGameResultScreen: (Int) -> Unit
) {
    val gridState: LazyGridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    val gameMode = viewModel.selectedGameMode.collectAsStateWithLifecycle()
    val flowGameUiState by viewModel.flowGameUiState.collectAsStateWithLifecycle()
    val isGameOverAnimating = remember { mutableStateOf(false) }

    val showPauseDialog = remember { mutableStateOf(false) }

    FlowGameScreenContent(
        gameMode = gameMode.value,
        gridState = gridState,
        flowGameUiState = flowGameUiState,
        sendEvent = { flowGameUiEvent -> viewModel.onEvent(flowGameUiEvent) },
        toGameResultScreen = { toGameResultScreen(flowGameUiState.score) },
        showPauseDialog = showPauseDialog.value,
        updatePauseDialogVisibility = { showPauseDialog.value = it },
        isGameOverAnimating = isGameOverAnimating.value
    )

    LaunchedEffect(key1 = true) {
        snapshotFlow { gridState.firstVisibleItemIndex }
            .collect {
                viewModel.onEvent(FlowGameUiEvent.FirstVisibleItemIndexChanged(gridState.firstVisibleItemIndex))
            }
    }

    if (flowGameUiState.isReady && !flowGameUiState.isActive && !flowGameUiState.isPaused) {
        // We handle ReadyToGameLayout inside FlowGameScreenContent to manage shared coordinates
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
            dialogTitle = stringResource(Res.string.title_pause_dialog),
            dialogText = stringResource(Res.string.text_pause_dialog),
            isSoundEnabled = flowGameUiState.isSoundEnabled,
            isMusicEnabled = flowGameUiState.isMusicEnabled,
            isVibrationEnabled = flowGameUiState.isVibrationEnabled,
            onSoundToggle = { viewModel.onEvent(FlowGameUiEvent.ToggleSound(it)) },
            onMusicToggle = { viewModel.onEvent(FlowGameUiEvent.ToggleMusic(it)) },
            onVibrationToggle = { viewModel.onEvent(FlowGameUiEvent.ToggleVibration(it)) }
        )
    }

    if (flowGameUiState.isFinished) {
        LaunchedEffect(key1 = true) {
            isGameOverAnimating.value = true
            delay(1000)
            toGameResultScreen(flowGameUiState.score)
        }
    } else if (flowGameUiState.isActive || flowGameUiState.isReady) {
        isGameOverAnimating.value = false
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
internal fun FlowGameScreenContent(
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    gameMode: GameMode = GameMode.FLOW,
    flowGameUiState: FlowGameUiState,
    sendEvent: (FlowGameUiEvent) -> Unit,
    toGameResultScreen: (Int) -> Unit = {},
    showPauseDialog: Boolean = false,
    updatePauseDialogVisibility: (Boolean) -> Unit = {},
    isGameOverAnimating: Boolean = false
) {
    val density = LocalDensity.current
    val flowGameScreenContentDesc = stringResource(Res.string.flow_game_screen_content_desc)

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

    val screenHeightPx = LocalWindowInfo.current.containerSize.height
    val screenHeight = with(density) { screenHeightPx.toDp() }
    var gameInfoPanelSize by remember {
        mutableStateOf(0.dp)
    }
    val spacerHeight by remember {
        derivedStateOf {
            if (gameInfoPanelSize == 0.dp) 0.dp else (screenHeight - gameInfoPanelSize).coerceAtLeast(0.dp)
        }
    }

    var targetGameInfoCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val infoPanelAlpha by animateFloatAsState(
        targetValue = if (flowGameUiState.isActive || isGameOverAnimating) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "infoPanelAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(DesignRes.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (flowGameUiState.isReady && !flowGameUiState.isActive && !flowGameUiState.isPaused) {
            ReadyToGameLayout(
                modifier = Modifier.zIndex(2f),
                goals = flowGameUiState.goals,
                goalsSuitableFigures = flowGameUiState.goalSuitableFigures,
                setGameReadyToStart = { sendEvent(FlowGameUiEvent.SetGameReadyToStart) },
                targetCoordinates = targetGameInfoCoordinates,
                isTransitioning = false, // FlowGameEngine might need a transition state, but for now we'll use a timer
                maxLifeCount = flowGameUiState.maxLifeCount,
                lifeCount = flowGameUiState.lifeCount,
                score = flowGameUiState.score,
                coefficient = flowGameUiState.coefficient,
                gameDuration = flowGameUiState.gameDuration
            )
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
                        .alpha(infoPanelAlpha)
                        .shake(enabled = flowGameUiState.isLifeWasted)
                        .flashRed(enabled = flowGameUiState.isLifeWasted)
                        .onGloballyPositioned {
                            targetGameInfoCoordinates = it
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

                if (flowGameUiState.isActive || isGameOverAnimating) {
                    FlowGameFieldLayout(
                        modifier = Modifier.shake(enabled = isGameOverAnimating),
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
                        reverseLayout = flowGameUiState.isReverseScrolling,
                        isGameOver = isGameOverAnimating
                    )
                }
            }
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
                val scrollValue = if (flowGameUiState.isReverseScrolling) {
                    -pixelsToScrollWithSpacers
                } else {
                    pixelsToScrollWithSpacers
                }
                gridState.animateScrollBy(
                    value = scrollValue,
                    animationSpec = tween(
                        durationMillis = flowGameUiState.scrollDuration,
                        easing = LinearEasing
                    )
                )
            }
        }
    }
}

@Composable
private fun ReadyToGameLayout(
    modifier: Modifier = Modifier,
    goals: Set<Goal<Any>>,
    goalsSuitableFigures: Set<Figure>,
    setGameReadyToStart: () -> Unit,
    targetCoordinates: LayoutCoordinates? = null,
    isTransitioning: Boolean = false,
    maxLifeCount: Int = 5,
    lifeCount: Int = 5,
    score: Int = 0,
    coefficient: Float = 0f,
    gameDuration: Long = 0
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var countDown by remember {
            mutableStateOf(3)
        }
        var isTimerFinished by remember { mutableStateOf(false) }

        val text = if (countDown > 0) "$countDown" else "Go!"

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GameInfoPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .moveAndScale(
                        targetCoordinates = targetCoordinates,
                        isAtTarget = isTimerFinished,
                        animationDuration = 500
                    ),
                maxLifeCount = maxLifeCount,
                lifeCount = lifeCount,
                goals = goals,
                goalsSuitableFigures = goalsSuitableFigures,
                score = score,
                coefficient = coefficient,
                gameDuration = gameDuration,
                showScoreAndTime = isTimerFinished,
                showCoefficient = isTimerFinished,
                showLives = isTimerFinished
            )
            Text(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .alpha(if (isTimerFinished) 0f else 1f),
                text = stringResource(Res.string.feature_gameplay_click_on_items),
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center
            )

            Text(
                text = text,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .alpha(if (isTimerFinished) 0f else 1f),
                style = MaterialTheme.typography.displayLarge
            )
        }

        LaunchedEffect(key1 = Unit) {
            repeat(3) {
                delay(1_000)
                countDown--
            }
            isTimerFinished = true
            delay(500)
            setGameReadyToStart()
        }
    }
}
