package com.maxot.seekandcatch.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.maxot.seekandcatch.feature.gameplay.ui.FlowGameScreenRoute
import com.maxot.seekandcatch.feature.score.ui.ScoreScreen
import com.maxot.seekandcatch.feature.settings.ui.SettingsScreen
import com.maxot.seekandcatch.ui.MainScreenRoute
import com.maxot.seekandcatch.ui.SeekAndCatchAppState

@Composable
fun SeekCatchNavHost(
    appState: SeekAndCatchAppState
) {
    val navController = appState.navController

    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(Screen.MainScreen.route) {
            MainScreenRoute { route ->
                navController.navigate(route = route)
            }
        }
        composable(Screen.ScoreScreen.route) {
            ScoreScreen {
                navController.navigate(Screen.MainScreen.route)
            }
        }
        composable(Screen.GameScreen.route) {
            FlowGameScreenRoute { navController.navigate(Screen.ScoreScreen.route) }
        }
        composable(Screen.SettingsScreen.route) {
            SettingsScreen()
        }
    }
}
