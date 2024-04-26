package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.feature.gameplay.FlowGameUiState
import com.maxot.seekandcatch.feature.gameplay.FlowGameViewModel
import com.maxot.seekandcatch.feature.gameplay.R

@Composable
fun GameSelectionScreenRoute(
    viewModel: FlowGameViewModel = hiltViewModel(),
    navigateToFlowGame: () -> Unit
) {
    val selectedDifficulty = viewModel.selectedGameDifficulty.collectAsState()

    GameSelectionScreen(
        navigateToFlowGame = navigateToFlowGame,
        selectedDifficulty = selectedDifficulty.value ?: GameDifficulty.NORMAL
    ) { difficulty ->
        viewModel.setSelectedDifficulty(difficulty)
    }
}

@Composable
fun GameSelectionScreen(
    modifier: Modifier = Modifier,
    selectedDifficulty: GameDifficulty,
    navigateToFlowGame: () -> Unit,
    onDifficultChanged: (GameDifficulty) -> Unit
) {
    val gameSelectionScreenContentDesc = stringResource(id = R.string.game_selection_screen_content_desc)
    val backgroundBrush =
        Brush.linearGradient(listOf(Color.Transparent, Color.Yellow, Color.Green, Color.Blue))

    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .background(backgroundBrush)
            .semantics { contentDescription = gameSelectionScreenContentDesc },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        StartGameLayout(
            modifier = Modifier.fillMaxWidth(),
            selectedDifficulty = selectedDifficulty,
            onDifficultyChanged = {
                onDifficultChanged(it)
            },
            onStartButtonClick = {
                navigateToFlowGame()
            })
    }
}

@Preview
@Composable
fun GameSelectionScreenPreview() {
    SeekAndCatchTheme {
        GameSelectionScreen(
            navigateToFlowGame = {},
            selectedDifficulty = GameDifficulty.HARD
        ) {

        }
    }
}