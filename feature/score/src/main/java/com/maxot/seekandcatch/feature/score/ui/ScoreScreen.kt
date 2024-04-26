package com.maxot.seekandcatch.feature.score.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxot.seekandcatch.feature.score.R
import com.maxot.seekandcatch.feature.score.ScoreViewModel

@Composable
fun ScoreScreen(
    viewModel: ScoreViewModel = hiltViewModel(),
    toMainScreen: () -> Unit
) {
    val score by rememberSaveable {
        mutableStateOf(viewModel.getLastScore())
    }
    val bestScore by rememberSaveable {
        mutableStateOf(viewModel.getBestScore())
    }

    val backgroundBrush =
        Brush.linearGradient(listOf(Color.Blue, Color.Transparent, Color.Cyan, Color.Transparent))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.label_score, score),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = stringResource(id = R.string.label_your_best_score, bestScore),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(5.dp))
        OutlinedButton(
            onClick = { toMainScreen() },

            ) {
            Text(
                text = stringResource(id = R.string.button_to_main_screen, score),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
        }

        OutlinedButton(
            onClick = {
                viewModel.addToLeaderboard(score)
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
}