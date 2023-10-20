package com.maxot.seekandcatch.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxot.seekandcatch.GameViewModel
import com.maxot.seekandcatch.R

@Composable
fun ScoreScreen(
    viewModel: GameViewModel = hiltViewModel(),
    toMainScreenClick: () -> Unit
) {
    val score = viewModel.lastScore
    val bestScore = viewModel.getBestScore()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.label_score, score),
            textAlign = TextAlign.Center,
            style = TextStyle(color = Color.Black, fontSize = 30.sp)
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = stringResource(id = R.string.label_your_best_score, bestScore),
            textAlign = TextAlign.Center,
            style = TextStyle(color = Color.Black, fontSize = 30.sp)
        )
        Spacer(modifier = Modifier.height(5.dp))
        Button(
            onClick = { toMainScreenClick },

            ) {
            Text(
                text = stringResource(id = R.string.button_to_main_screen, score),
                textAlign = TextAlign.Center,
                style = TextStyle(color = Color.Black, fontSize = 30.sp)
            )
        }
    }
}