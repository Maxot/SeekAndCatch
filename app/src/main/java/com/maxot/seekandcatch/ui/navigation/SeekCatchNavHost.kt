package com.maxot.seekandcatch.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.maxot.seekandcatch.feature.gameplay.ui.FlowGameScreen
import com.maxot.seekandcatch.feature.gameplay.ui.FlowModeGameScreen
import com.maxot.seekandcatch.feature.gameplay.ui.GameScreen
import com.maxot.seekandcatch.feature.score.ui.ScoreScreen
import com.maxot.seekandcatch.ui.MainScreen
import com.maxot.seekandcatch.ui.SeekAndCatchAppState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SeekCatchNavHost(
    appState: SeekAndCatchAppState
) {
    val navController = appState.navController

    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(Screen.MainScreen.route) {
            MainScreen { route ->
                navController.navigate(route = route)
            }
        }
        composable(Screen.ScoreScreen.route) {
            ScoreScreen {
                navController.navigate(Screen.MainScreen.route)
            }
        }
        composable(Screen.FlowGameScreen.route) {
            FlowModeGameScreen(
                toScoreScreen = { navController.navigate(Screen.ScoreScreen.route) }
            )
        }
        composable(Screen.FrameGameScreen.route) {
            GameScreen(
                toScoreScreen = { navController.navigate(Screen.ScoreScreen.route) }
            )
        }
        composable(Screen.GameScreen.route) {
            FlowGameScreen(
                toScoreScreen = { navController.navigate(Screen.ScoreScreen.route) }
            )
        }
    }
}
