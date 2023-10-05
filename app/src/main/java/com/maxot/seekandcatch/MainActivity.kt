package com.maxot.seekandcatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maxot.seekandcatch.ui.GameScreen
import com.maxot.seekandcatch.ui.MainScreen
import com.maxot.seekandcatch.ui.NextLevelScreen
import com.maxot.seekandcatch.ui.ScoreScreen
import com.maxot.seekandcatch.ui.theme.SeekAndCatchTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeekAndCatchTheme {
                val viewModel = viewModel<MainActivityViewModel>()
                val scope = rememberCoroutineScope()
                val isGameActive = viewModel.isGameActive.collectAsState()
                val lastScore = viewModel.lastScore.collectAsState()
                val goals = viewModel.goals.collectAsState()
                val score = viewModel.score.collectAsState()
                val level = viewModel.level.collectAsState()
                val isLevelChanged = viewModel.levelChanged.collectAsState()

                if (!isGameActive.value) {
                    if (lastScore.value > 0) {
                        ScoreScreen(
                            score = lastScore.value,
                            bestScore = viewModel.getBestScore(),
                            onStartGameClick = {
                                scope.launch { viewModel.startGame() }
                            })
                    } else {
                        MainScreen(viewModel.getBestScore()) {
                            scope.launch { viewModel.startGame() }
                        }
                    }
                } else {
                    if (isLevelChanged.value) {
                        NextLevelScreen(nextLevel = level.value, goals = goals)
                    } else {
                        GameScreen(
                            goals = goals,
                            figures = viewModel.figures.collectAsState(),
                            score = score,
                            onFigureClickAction = { figure ->
                                val condition = viewModel.gameplayUseCase.checkGoalCondition(
                                    goals = goals.value,
                                    figure = figure
                                )
                                if (condition) {
                                    viewModel.incrementScore()
                                } else {
                                    viewModel.stopGame()
                                }
                            })
                    }
                }
            }
        }
    }
}
