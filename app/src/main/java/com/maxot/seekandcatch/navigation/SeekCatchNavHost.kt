package com.maxot.seekandcatch.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.maxot.seekandcatch.feature.gameplay.navigation.GAME_SELECTION_ROUTE
import com.maxot.seekandcatch.feature.gameplay.navigation.gameSelectionScreen
import com.maxot.seekandcatch.feature.gameplay.navigation.navigateToFlowGame
import com.maxot.seekandcatch.feature.gameplay.navigation.navigateToGameSelection
import com.maxot.seekandcatch.feature.leaderboard.navigation.leaderboardScreen
import com.maxot.seekandcatch.feature.score.navigation.navigateToScore
import com.maxot.seekandcatch.feature.settings.navigation.settingsScreen
import com.maxot.seekandcatch.ui.SeekAndCatchAppState

@Composable
fun SeekCatchNavHost(
    appState: SeekAndCatchAppState,
    modifier: Modifier = Modifier,
) {
    val navController = appState.navController

    NavHost(
        modifier = Modifier.then(modifier),
        navController = navController,
        startDestination = GAME_SELECTION_ROUTE
    ) {
        leaderboardScreen()
        gameSelectionScreen(
            navigateToFlowGame = navController::navigateToFlowGame,
            navigateToScore = navController::navigateToScore,
            navigateToGameSelection = navController::navigateToGameSelection
        )
        settingsScreen()

    }

}
