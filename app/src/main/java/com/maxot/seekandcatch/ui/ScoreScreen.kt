package com.maxot.seekandcatch.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun ScoreScreen(score: Int, onStartGameClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(
            text = "Your score: $score",
            textAlign = TextAlign.Center,
            style = TextStyle(color = Color.Black, fontSize = 30.sp)
        )
        Button(
            onClick = { onStartGameClick() },

            ) {
            Text(
                text = "Start new game",
                textAlign = TextAlign.Center,
                style = TextStyle(color = Color.Black, fontSize = 30.sp)
            )
        }
    }
}