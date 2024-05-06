package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxot.seekandcatch.feature.gameplay.GameResultViewModel
import com.maxot.seekandcatch.feature.gameplay.R

@Composable
fun GameResultScreen(
    viewModel: GameResultViewModel = hiltViewModel(),
    toMainScreen: () -> Unit,
) {
    val lastScore by rememberSaveable {
        mutableStateOf(viewModel.getLastScore())
    }
    val bestScore by rememberSaveable {
        mutableStateOf(viewModel.getBestScore())
    }
    GameResultScreen(
        lastScore = lastScore,
        bestScore = bestScore,
        toMainScreen = toMainScreen,
        addResultToLeaderboard = viewModel::addResultToLeaderboard
    )
}

@Composable
fun GameResultScreen(
    lastScore: Int,
    bestScore: Int,
    toMainScreen: () -> Unit,
    addResultToLeaderboard: (Int) -> Unit
) {
    val isNewBest by remember {
        derivedStateOf {
            lastScore > bestScore
        }
    }

    val backgroundBrush =
        Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.secondary,
                Color.Transparent,
                MaterialTheme.colorScheme.onSecondary,
                Color.Transparent
            )
        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isNewBest) {
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
                Text(
                    text = stringResource(id = R.string.feature_gameplay_label_score, lastScore),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
                OutlinedButton(
                    onClick = {
                        addResultToLeaderboard(lastScore)
                        toMainScreen()
                    },
                ) {
                    Text(
                        text = "Add to leaderboard",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        } else {
            Text(
                text = stringResource(id = R.string.feature_gameplay_label_score, lastScore),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = stringResource(
                    id = R.string.feature_gameplay_label_your_best_score,
                    bestScore
                ),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(5.dp))
            OutlinedButton(
                onClick = { toMainScreen() },

                ) {
                Text(
                    text = stringResource(
                        id = R.string.feature_gameplay_button_to_main_screen,
                        lastScore
                    ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Preview
@Composable
fun GameResultScreenPreview() {
    GameResultScreen(
        lastScore = 5,
        bestScore = 15,
        toMainScreen = {},
        addResultToLeaderboard = {}
    )
}

@Preview
@Composable
fun GameResultScreenNewBestPreview() {
    GameResultScreen(
        lastScore = 20,
        bestScore = 15,
        toMainScreen = {},
        addResultToLeaderboard = {}
    )
}