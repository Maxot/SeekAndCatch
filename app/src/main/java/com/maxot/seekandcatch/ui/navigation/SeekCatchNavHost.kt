package com.maxot.seekandcatch.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.maxot.seekandcatch.ui.FlowModeGameScreen
import com.maxot.seekandcatch.ui.GameScreen
import com.maxot.seekandcatch.ui.MainScreen
import com.maxot.seekandcatch.ui.ScoreScreen
import com.maxot.seekandcatch.ui.SeekAndCatchAppState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SeekCatchNavHost(
    appState: SeekAndCatchAppState
) {
    val navController = appState.navController

    NavHost(navController = navController, startDestination = Screen.MainScreen.route){
        composable(Screen.MainScreen.route){
            MainScreen{ route ->
                navController.navigate(route = route)
            }
        }
        composable(Screen.ScoreScreen.route){
            ScoreScreen{
                navController.navigate(Screen.MainScreen.route)
            }
        }
        composable(Screen.FlowGameScreen.route){
            FlowModeGameScreen()
        }
        composable(Screen.FrameGameScreen.route){
            GameScreen()
        }
    }
//     Need to fix this
//    if (gameUiState.value == GameViewModel.GameUiState.GameEnded){
//        navController.navigate(Screen.ScoreScreen.route)
//    }


}