package com.maxot.seekandcatch.feature.gameplay.ui.flashgame

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.feature.gameplay.R
import com.maxot.seekandcatch.feature.gameplay.ui.PauseDialog
import com.maxot.seekandcatch.feature.gameplay.ui.flashgame.model.FlashGameUiState
import com.maxot.seekandcatch.feature.gameplay.ui.layout.FlashGameFieldLayout
import com.maxot.seekandcatch.feature.gameplay.ui.layout.GameInfoPanel

@Composable
fun FlashGameScreen(
    viewModel: FlashGameViewModel = hiltViewModel(),
    toGameResultScreen: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FlashGameScreenContent(
        uiState = uiState,
        onStart = { viewModel.startGame() },
        onPause = { viewModel.pauseGame() },
        onResume = { viewModel.resumeGame() },
        onFinish = { viewModel.finishGame(); toGameResultScreen() },
        onCellClick = { id -> viewModel.onCellClick(id) }
    )

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            toGameResultScreen()
        }
    }
}

@Composable
private fun FlashGameScreenContent(
    uiState: FlashGameUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit,
    onCellClick: (Int) -> Unit,
) {
    val contentDesc = stringResource(id = R.string.flow_game_screen_content_desc)

    BackHandler(enabled = uiState.isActive) {
        onPause()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = com.maxot.seekandcatch.core.designsystem.R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = contentDesc },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (uiState.isLoading) {
                Text(
                    text = stringResource(id = R.string.feature_gameplay_loading),
                    style = MaterialTheme.typography.titleLarge
                )
            } else if (uiState.isReady && !uiState.isActive) {
                // Show goals and countdown before start
                GameInfoPanel(
                    maxLifeCount = 5,
                    lifeCount = uiState.lifeCount,
                    goals = uiState.goals,
                    goalsSuitableFigures = uiState.goalSuitableFigures,
                    score = uiState.score,
                    coefficient = 0f,
                    gameDuration = uiState.gameDuration,
                )

                ReadyToFlashGameLayout(
                    goalsSuitableFigures = uiState.goalSuitableFigures,
                    onCountdownFinished = onStart
                )
            } else {
                GameInfoPanel(
                    maxLifeCount = 5,
                    lifeCount = uiState.lifeCount,
                    goals = uiState.goals,
                    goalsSuitableFigures = uiState.goalSuitableFigures,
                    score = uiState.score,
                    coefficient = 0f,
                    gameDuration = uiState.gameDuration,
                )
                FlashGameFieldLayout(
                    modifier = Modifier.padding(16.dp),
                    gridWidth = uiState.gridWidth,
                    gridSize = uiState.gridSize,
                    figuresByCell = uiState.figuresByCell,
                    visibleCells = uiState.visibleCells,
                    onCellClick = onCellClick
                )
            }
        }
    }

    if (uiState.isPaused) {
        PauseDialog(
            onDismissRequest = {
                onResume()
            },
            onConfirmation = {
                onFinish()
            },
            dialogTitle = stringResource(id = R.string.title_pause_dialog),
            dialogText = stringResource(id = R.string.text_pause_dialog)
        )
    }
}

@Composable
private fun FlashGameCell(
    id: Int,
    visible: Boolean,
    onClick: () -> Unit,
) {
    // Minimal placeholder: show a colored tile when visible, text otherwise
    Card(
        modifier = Modifier
            .padding(4.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (visible) {
                Text(text = "✨", style = MaterialTheme.typography.titleLarge)
            } else {
                Text(text = "\u25A1", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun ReadyToFlashGameLayout(
    goalsSuitableFigures: Set<Figure>,
    onCountdownFinished: () -> Unit
) {
    var countDown by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(3) }
    val text = if (countDown > 0) "$countDown" else "Go!"

    com.maxot.seekandcatch.feature.gameplay.ui.layout.DetailedGoalsLayout(
        goalsSuitableFigures = goalsSuitableFigures
    )
    Text(
        modifier = Modifier.padding(top = 20.dp),
        text = stringResource(R.string.feature_gameplay_click_on_items),
        style = MaterialTheme.typography.displayLarge,
        textAlign = TextAlign.Center
    )

    LaunchedEffect(key1 = Unit) {
        kotlinx.coroutines.delay(1_000)
        repeat(2) {
            countDown--
            kotlinx.coroutines.delay(1_000)
        }
        countDown--
        kotlinx.coroutines.delay(500)
        onCountdownFinished()
    }

    Text(
        text = text,
        modifier = Modifier.padding(top = 20.dp),
        style = MaterialTheme.typography.displayLarge
    )
}

/**
 * Previews
 */

@Preview(showBackground = true)
@Composable
private fun FlashGameScreenLoadingPreview() {
    SeekAndCatchTheme {
        FlashGameScreenContent(
            uiState = FlashGameUiState(isLoading = true),
            onStart = {},
            onPause = {},
            onResume = {},
            onFinish = {},
            onCellClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FlashGameScreenReadyPreview() {
    val suitable = setOf(
        Figure.getRandomFigure(1),
        Figure.getRandomFigure(2),
        Figure.getRandomFigure(3),
    )
    SeekAndCatchTheme {
        FlashGameScreenContent(
            uiState = FlashGameUiState(
                isLoading = false,
                isReady = true,
                isActive = false,
                lifeCount = 3,
                score = 0,
                goalSuitableFigures = suitable,
                goals = emptySet()
            ),
            onStart = {},
            onPause = {},
            onResume = {},
            onFinish = {},
            onCellClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FlashGameScreenActivePreview() {
    val gridSize = 16
    val figuresByCell = (0 until gridSize).associateWith { id -> Figure.getRandomFigure(id) }
    val visible = setOf(1, 5, 7, 12)
    SeekAndCatchTheme {
        FlashGameScreenContent(
            uiState = FlashGameUiState(
                isLoading = false,
                isReady = false,
                isActive = true,
                gridSize = gridSize,
                gridWidth = 4,
                visibleCells = visible,
                figuresByCell = figuresByCell,
                lifeCount = 3,
                score = 42
            ),
            onStart = {},
            onPause = {},
            onResume = {},
            onFinish = {},
            onCellClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FlashGameScreenPausedPreview() {
    val gridSize = 16
    val figuresByCell = (0 until gridSize).associateWith { id -> Figure.getRandomFigure(id) }
    SeekAndCatchTheme {
        FlashGameScreenContent(
            uiState = FlashGameUiState(
                isLoading = false,
                isReady = false,
                isActive = false,
                isPaused = true,
                gridSize = gridSize,
                gridWidth = 4,
                visibleCells = emptySet(),
                figuresByCell = figuresByCell,
                lifeCount = 2,
                score = 100
            ),
            onStart = {},
            onPause = {},
            onResume = {},
            onFinish = {},
            onCellClick = {}
        )
    }
}
