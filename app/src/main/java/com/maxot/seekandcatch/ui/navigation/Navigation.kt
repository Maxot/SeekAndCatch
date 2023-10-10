package com.maxot.seekandcatch.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.maxot.seekandcatch.MainActivityViewModel
import com.maxot.seekandcatch.ui.FlowModeGameScreen
import com.maxot.seekandcatch.ui.GameScreen
import com.maxot.seekandcatch.ui.MainScreen
import com.maxot.seekandcatch.ui.ScoreScreen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Navigation(viewModel: MainActivityViewModel){
    val navController = rememberNavController()
    val isGameActive = viewModel.isGameActive.collectAsState()

    NavHost(navController = navController, startDestination = Screen.MainScreen.route){
        composable(Screen.MainScreen.route){
            MainScreen(bestScore = viewModel.getBestScore(), navController = navController)
        }
        composable(Screen.ScoreScreen.route){
            ScoreScreen(navController = navController, bestScore = viewModel.getBestScore(), score = 0)
        }
        composable(Screen.FlowGameScreen.route){
            FlowModeGameScreen(viewModel)
        }
        composable(Screen.FrameGameScreen.route){
            GameScreen(navController, viewModel)
        }
    }
    // Need to fix this
    if (isGameActive.value == false){
        navController.navigate(Screen.ScoreScreen.route)
    }
}
