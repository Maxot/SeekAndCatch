package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.data.model.GameMode
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.feature.gameplay.GameSelectionViewModel
import com.maxot.seekandcatch.feature.gameplay.R
import com.maxot.singleselectionlazyrow.SingleSelectionLazyRow
import com.maxot.seekandcatch.feature.gameplay.ui.layout.StartGameLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun GameSelectionScreenRoute(
    viewModel: GameSelectionViewModel = hiltViewModel(),
    navigateToFlowGame: () -> Unit
) {
    val selectedDifficulty = viewModel.selectedGameDifficulty.collectAsState()
    val selectedGameMode = viewModel.selectedGameMode.collectAsState()

    GameSelectionScreen(
        navigateToFlowGame = navigateToFlowGame,
        selectedDifficulty = selectedDifficulty.value ?: GameDifficulty.NORMAL,
        onDifficultChanged = { difficulty ->
            viewModel.setSelectedDifficulty(difficulty)
        },
        selectedMode = selectedGameMode.value ?: GameMode.FLOW,
        onSelectedModeChanged = { gameMode ->
            viewModel.setSelectedGameMode(gameMode)
        }
    )
}

@Composable
fun GameSelectionScreen(
    modifier: Modifier = Modifier,
    navigateToFlowGame: () -> Unit,
    selectedDifficulty: GameDifficulty,
    onDifficultChanged: (GameDifficulty) -> Unit,
    selectedMode: GameMode,
    onSelectedModeChanged: (GameMode) -> Unit
) {
    val gameSelectionScreenContentDesc =
        stringResource(id = R.string.feature_gameplay_game_selection_screen_content_desc)

    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .semantics { contentDescription = gameSelectionScreenContentDesc },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ModeSelectionLayout(
            selectedMode = selectedMode,
            onSelectedModeChanged = onSelectedModeChanged
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )

        StartGameLayout(
            modifier = Modifier
                .fillMaxWidth(),
            selectedDifficulty = selectedDifficulty,
            onDifficultyChanged = {
                onDifficultChanged(it)
            },
            onStartButtonClick = {
                navigateToFlowGame()
            })
    }
}

@Composable
fun ModeSelectionLayout(
    selectedMode: GameMode,
    onSelectedModeChanged: (GameMode) -> Unit
) {
    val gameModes = GameMode.entries.toTypedArray()

    SingleSelectionLazyRow(
        items = gameModes.toList(),
        selectedItemIndex = GameMode.entries.indexOf(selectedMode),
        onSelectedItemChanged = { index ->
            onSelectedModeChanged(gameModes[index])
        }) { modifier, gameMode ->
        FlowGamePreviewCard(id = gameMode.ordinal, gameMode = gameMode, modifier = modifier)
    }
}

@Composable
fun FlowGamePreviewCard(
    id: Int,
    gameMode: GameMode,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    gridState: LazyGridState = rememberLazyGridState()
) {
    val figures = remember {
        val list = mutableListOf<Figure>()
        repeat(100) {
            list.add(Figure.getRandomFigure(it))
        }
        list
    }

    ElevatedCard(
        modifier = Modifier
            .then(modifier)
            .height(350.dp)
            .width(250.dp)
            .padding(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
    ) {
        Text(
            text = gameMode.name,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        GameFieldLayout(
            spacerHeight = 300.dp,
            figures = figures.toList(),
            gridState = gridState,
            onItemClick = { 0 },
            reverseLayout = gameMode == GameMode.DROP
        )
        LaunchedEffect(key1 = true) {
            coroutineScope.launch {
                while (true) {
                    gridState.animateScrollBy(
                        value = 5000f,
                        animationSpec = tween(
                            durationMillis = 20000,
                            easing = LinearEasing
                        )
                    )
                    gridState.scrollToItem(0)
                }
            }

        }
    }

}


@Preview
@Composable
fun GameSelectionScreenPreview() {
    SeekAndCatchTheme {
        GameSelectionScreen(
            navigateToFlowGame = {},
            selectedDifficulty = GameDifficulty.HARD,
            selectedMode = GameMode.FLOW,
            onDifficultChanged = {},
            onSelectedModeChanged = {}
        )
    }
}