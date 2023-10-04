package com.maxot.seekandcatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maxot.seekandcatch.ui.GameScreen
import com.maxot.seekandcatch.ui.MainScreen
import com.maxot.seekandcatch.ui.ScoreScreen
import com.maxot.seekandcatch.ui.theme.SeekAndCatchTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeekAndCatchTheme {
                val viewModel = viewModel<MainActivityViewModel>()
                val scope = rememberCoroutineScope()
                val isGameActive = viewModel.isGameActive.collectAsState()
                val lastScore = viewModel.lastScore.collectAsState()

                if (!isGameActive.value) {
                    if (lastScore.value > 0) {
                        ScoreScreen(score = lastScore.value, onStartGameClick = {
                            scope.launch { viewModel.startGame() }
                        })
                    } else {
                        MainScreen {
                            scope.launch { viewModel.startGame() }
                        }
                    }
                } else {
                    GameScreen(
                        goal = viewModel.goal,
                        figures = viewModel.figures.collectAsState(),
                        onStopGame = { score -> viewModel.stopGame(score) })
                }
            }
        }
    }
}
