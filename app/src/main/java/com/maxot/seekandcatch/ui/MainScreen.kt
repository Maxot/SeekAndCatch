package com.maxot.seekandcatch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxot.seekandcatch.R
import com.maxot.seekandcatch.feature.gameplay.GameViewModel
import com.maxot.seekandcatch.ui.navigation.Screen

@Composable
fun MainScreen(
    viewModel: GameViewModel = hiltViewModel(),
    navigateToOtherScreen: (route: String) -> Unit
) {
    val bestScore = viewModel.getBestScore()

    val backgroundBrush =
        Brush.linearGradient(listOf(Color.Transparent, Color.Yellow, Color.Green, Color.Blue))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .semantics { contentDescription = "Main Screen" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
//        Box(
//            modifier = Modifier
//                .size(100.dp)
//                .clip(CircleShape)
//                .background(MaterialTheme.colorScheme.onPrimary)
//        )
//        Text(
//            text = stringResource(id = R.string.label_your_best_score, bestScore),
//            style = TextStyle(fontSize = 30.sp)
//        )
//        Spacer(modifier = Modifier.size(50.dp))
//        TextButton(
//            onClick = { navigateToOtherScreen(Screen.ScoreScreen.route) },
//        ) {
//            Text(
//                text = "Score Screen",
//                textAlign = TextAlign.Center,
//                style = TextStyle(color = Color.Black, fontSize = 30.sp)
//            )
//        }
//        Spacer(modifier = Modifier.size(50.dp))
//        Button(
//            onClick = { navigateToOtherScreen(Screen.FrameGameScreen.route) },
//
//            ) {
//            Text(
//                text = stringResource(id = R.string.button_start_game),
//                textAlign = TextAlign.Center,
//                style = TextStyle(color = Color.Black, fontSize = 30.sp)
//            )
//        }
//        Spacer(modifier = Modifier.size(50.dp))
//        Button(
//            onClick = { navigateToOtherScreen(Screen.FlowGameScreen.route) },
//
//            ) {
//            Text(
//                text = "Start flow mode game",
//                textAlign = TextAlign.Center,
//                style = TextStyle(color = Color.Black, fontSize = 30.sp)
//            )
//        }
        Spacer(modifier = Modifier.size(50.dp))
        TextButton(
            onClick = { navigateToOtherScreen(Screen.GameScreen.route) },

            ) {
            Text(
                text = "Start Game",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayLarge
            )
        }
    }
}