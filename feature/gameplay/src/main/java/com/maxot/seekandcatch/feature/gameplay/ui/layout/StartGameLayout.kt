package com.maxot.seekandcatch.feature.gameplay.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.designsystem.ui.PixelButton
import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.feature.gameplay.R

@Composable
fun StartGameLayout(
    modifier: Modifier = Modifier,
    selectedDifficulty: GameDifficulty,
    onDifficultyChanged: (GameDifficulty) -> Unit,
    onStartButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier.then(modifier),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PixelButton(
            modifier = Modifier.padding(20.dp),
            onClick = { onStartButtonClick() },
        ) {
            Text(
                text = stringResource(id = R.string.start_game_button_text),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleLarge
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

@Preview(showBackground = true)
@Composable
fun StartGameLayoutPreview() {
    SeekAndCatchTheme {
        StartGameLayout(
            selectedDifficulty = GameDifficulty.NORMAL,
            onDifficultyChanged = {}) {
        }
    }
}