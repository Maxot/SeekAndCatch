package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.maxot.seekandcatch.data.model.GameDifficulty

@Composable
fun StartGameLayout(
    modifier: Modifier,
    selectedDifficulty: GameDifficulty,
    onDifficultyChanged: (GameDifficulty) -> Unit,
    onStartButtonClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(
            onClick = { onStartButtonClick() },

            ) {
            Text(
                text = "Start Game",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayLarge
            )
        }
        GameDifficultSelectorLayout(
            modifier = Modifier,
            variants = GameDifficulty.entries,
            defaultVariant = selectedDifficulty,
            onDifficultChanged = { gameDifficulty ->
                onDifficultyChanged(gameDifficulty)
            }
        )
    }
}