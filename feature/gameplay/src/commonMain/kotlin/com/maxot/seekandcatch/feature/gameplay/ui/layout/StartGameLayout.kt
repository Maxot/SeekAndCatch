package com.maxot.seekandcatch.feature.gameplay.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.core.designsystem.component.PixelButton
import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.feature.gameplay.generated.resources.Res
import com.maxot.seekandcatch.feature.gameplay.generated.resources.*
import org.jetbrains.compose.resources.stringResource

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
                text = stringResource(Res.string.start_game_button_text),
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
