package com.maxot.seekandcatch.ui

import androidx.compose.foundation.layout.*
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
import com.maxot.seekandcatch.R

@Composable
fun ScoreScreen(
    bestScore: Int,
    score: Int,
    onStartGameClick: () -> Unit
) {
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
            onClick = { onStartGameClick() },

            ) {
            Text(
                text = stringResource(id = R.string.button_start_new_game, score),
                textAlign = TextAlign.Center,
                style = TextStyle(color = Color.Black, fontSize = 30.sp)
            )
        }
    }
}