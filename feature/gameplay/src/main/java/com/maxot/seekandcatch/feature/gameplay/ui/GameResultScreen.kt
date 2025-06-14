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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.designsystem.ui.PixelButton
import com.maxot.seekandcatch.feature.account.ui.UserNameDialog
import com.maxot.seekandcatch.feature.gameplay.GameResultViewModel
import com.maxot.seekandcatch.feature.gameplay.R

@Composable
fun GameResultScreen(
    viewModel: GameResultViewModel = hiltViewModel(),
    toMainScreen: () -> Unit,
) {
    val userName by viewModel.userName.collectAsState()

    val lastScore by rememberSaveable {
        mutableStateOf(viewModel.getLastScore())
    }
    val bestScore by rememberSaveable {
        mutableStateOf(viewModel.getBestScore())
    }

    GameResultScreenBody(
        userName = userName,
        lastScore = lastScore,
        bestScore = bestScore,
        toMainScreen = toMainScreen,
        processNewBestScore = viewModel::processNewBestScore
    )
}

@Composable
private fun GameResultScreenBody(
    userName: String = "",
    lastScore: Int,
    bestScore: Int,
    toMainScreen: () -> Unit,
    processNewBestScore: (Int, Boolean) -> Unit
) {
    val showUserNameDialog = remember {
        mutableStateOf(false)
    }
    val isNewBest by remember {
        derivedStateOf {
            lastScore > bestScore
        }
    }

    if (showUserNameDialog.value) {
        UserNameDialog(
            onConfirmation = { showUserNameDialog.value = false },
            onDismissRequest = { showUserNameDialog.value = false }
        )
    }

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
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = stringResource(
                            id = R.string.feature_gameplay_label_score,
                            lastScore
                        ),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    PixelButton(
                        onClick = {
                            if (userName.isEmpty()) {
                                showUserNameDialog.value = true
                            } else {
                                processNewBestScore(lastScore, true)
                                toMainScreen()
                            }
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
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(
                        id = R.string.feature_gameplay_label_your_best_score,
                        bestScore
                    ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            PixelButton(
                onClick = {
                    processNewBestScore(lastScore, false)
                    toMainScreen()
                },
            ) {
                Text(
                    text = stringResource(
                        id = R.string.feature_gameplay_button_to_main_screen,
                        lastScore
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
            lastScore = 5,
            bestScore = 15,
            toMainScreen = {},
            processNewBestScore = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GameResultScreenNewBestPreview() {
    SeekAndCatchTheme {
        GameResultScreenBody(
            lastScore = 20,
            bestScore = 15,
            toMainScreen = {},
            processNewBestScore = { _, _ -> }
        )
    }

}