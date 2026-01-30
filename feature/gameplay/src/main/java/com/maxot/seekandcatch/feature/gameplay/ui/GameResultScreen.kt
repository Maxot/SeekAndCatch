package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.designsystem.component.PixelButton
import com.maxot.seekandcatch.feature.gameplay.GameResultEvent
import com.maxot.seekandcatch.feature.gameplay.GameResultUiState
import com.maxot.seekandcatch.feature.gameplay.GameResultViewModel
import com.maxot.seekandcatch.feature.gameplay.R

@Composable
fun GameResultScreen(
    viewModel: GameResultViewModel = hiltViewModel(),
    toMainScreen: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GameResultScreenBody(
        uiState = uiState,
        onContinue = {
            viewModel.onEvent(GameResultEvent.ContinueClicked)
            toMainScreen()
        }
    )
}

@Composable
private fun GameResultScreenBody(
    uiState: GameResultUiState,
    onContinue: () -> Unit,
) {
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uiState.isNewBest) {
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier,
                        text = "New best!",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            Text(
                text = stringResource(
                    id = R.string.feature_gameplay_label_score,
                    uiState.lastScore
                ),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(20.dp))
            if (!uiState.isNewBest) {
                Text(
                    text = stringResource(
                        id = R.string.feature_gameplay_label_your_best_score,
                        uiState.remoteBestForContext
                    ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
            PixelButton(
                onClick = { onContinue() },
            ) {
                Text(
                    text = stringResource(
                        id = R.string.feature_gameplay_button_to_main_screen,
                        uiState.lastScore
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GameResultScreenPreview() {
    SeekAndCatchTheme {
        GameResultScreenBody(
            uiState = GameResultUiState(lastScore = 5, remoteBestForContext = 15),
            onContinue = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GameResultScreenNewBestPreview() {
    SeekAndCatchTheme {
        GameResultScreenBody(
            uiState = GameResultUiState(lastScore = 20, remoteBestForContext = 15),
            onContinue = {}
        )
    }
}