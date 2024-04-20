package com.maxot.seekandcatch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxot.seekandcatch.MainViewModel
import com.maxot.seekandcatch.R
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.feature.gameplay.ui.StartGameLayout
import com.maxot.seekandcatch.navigation.Screen

@Composable
fun MainScreenRoute(
    viewModel: MainViewModel = hiltViewModel(),
    navigateToOtherScreen: (route: String) -> Unit
) {
    val selectedDifficulty = viewModel.selectedGameDifficulty.collectAsState()

    MainScreen(
        navigateToOtherScreen = navigateToOtherScreen,
        selectedDifficulty = selectedDifficulty.value
    ) { difficulty ->
        viewModel.setSelectedDifficulty(difficulty)
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    selectedDifficulty: GameDifficulty,
    navigateToOtherScreen: (route: String) -> Unit,
    onDifficultChanged: (GameDifficulty) -> Unit
) {
    val mainScreenContentDesc = stringResource(id = R.string.main_screen_content_desc)
    val backgroundBrush =
        Brush.linearGradient(listOf(Color.Transparent, Color.Yellow, Color.Green, Color.Blue))

    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .background(backgroundBrush)
            .semantics { contentDescription = mainScreenContentDesc },
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
                navigateToOtherScreen(Screen.GameScreen.route)
            })
        Spacer(modifier = Modifier.size(50.dp))
        TextButton(
            onClick = { navigateToOtherScreen(Screen.SettingsScreen.route) },

            ) {
            Text(
                text = "Settings",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    SeekAndCatchTheme {
        MainScreen(
            navigateToOtherScreen = {},
            selectedDifficulty = GameDifficulty.HARD
        ) {

        }
    }
}