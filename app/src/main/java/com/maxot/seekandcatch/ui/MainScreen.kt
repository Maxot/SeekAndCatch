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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxot.seekandcatch.MainViewModel
import com.maxot.seekandcatch.feature.gameplay.ui.StartGameLayout
import com.maxot.seekandcatch.ui.navigation.Screen

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    navigateToOtherScreen: (route: String) -> Unit
) {
    val selectedDifficulty = viewModel.selectedGameDifficulty.collectAsState()
    val backgroundBrush =
        Brush.linearGradient(listOf(Color.Transparent, Color.Yellow, Color.Green, Color.Blue))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .semantics { contentDescription = "Main Screen" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        StartGameLayout(modifier = Modifier.fillMaxWidth(),
            selectedDifficulty = selectedDifficulty.value,
            onDifficultyChanged = {
                viewModel.setSelectedDifficulty(it)
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