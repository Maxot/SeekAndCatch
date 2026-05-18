package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.designsystem.component.PixelButton
import com.maxot.seekandcatch.feature.gameplay.GameResultEvent
import com.maxot.seekandcatch.feature.gameplay.GameResultUiState
import com.maxot.seekandcatch.feature.gameplay.GameResultViewModel
import com.maxot.seekandcatch.feature.gameplay.R

@Composable
fun GameResultScreen(
    viewModel: GameResultViewModel = koinViewModel(),
    toMainScreen: () -> Unit,
    onRestart: (com.maxot.seekandcatch.core.common.model.GameMode) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isNewBest) {
        if (uiState.isNewBest && !uiState.hasPlayedNewBestSound) {
            viewModel.onEvent(GameResultEvent.NewBestSoundPlayed)
        }
    }

    var isRestartClicked by remember { mutableStateOf(false) }

    GameResultScreenBody(
        uiState = uiState,
        onContinue = {
            if (!isRestartClicked) {
                viewModel.onEvent(GameResultEvent.ContinueClicked)
                toMainScreen()
            }
        },
        onRestart = {
            if (!isRestartClicked) {
                isRestartClicked = true
                viewModel.onEvent(GameResultEvent.RestartClicked)
                uiState.gameMode?.let {
                    if (it != com.maxot.seekandcatch.core.common.model.GameMode.DROP) {
                        onRestart(it)
                    } else {
                        toMainScreen()
                    }
                }
            }
        }
    )
}

@Composable
private fun GameResultScreenBody(
    uiState: GameResultUiState,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
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
                val infiniteTransition = rememberInfiniteTransition(label = "scale")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.scale(scale),
                        text = "NEW RECORD!",
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
            uiState.rank?.let { rank ->
                Text(
                    text = stringResource(
                        id = R.string.feature_gameplay_label_rank,
                        rank
                    ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
            PixelButton(
                onClick = { onRestart() },
            ) {
                Text(
                    text = stringResource(id = R.string.feature_gameplay_button_restart),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
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
            onContinue = {},
            onRestart = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GameResultScreenNewBestPreview() {
    SeekAndCatchTheme {
        GameResultScreenBody(
            uiState = GameResultUiState(lastScore = 20, remoteBestForContext = 15),
            onContinue = {},
            onRestart = {}
        )
    }
}