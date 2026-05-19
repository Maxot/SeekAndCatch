package com.maxot.seekandcatch

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.feature.account.navigation.ACCOUNT_ROUTE
import com.maxot.seekandcatch.feature.account.navigation.accountScreen
import com.maxot.seekandcatch.feature.account.navigation.navigateToAccount
import com.maxot.seekandcatch.feature.gameplay.navigation.GAME_MAIN_ROUTE
import com.maxot.seekandcatch.feature.gameplay.navigation.gameSelectionScreen
import com.maxot.seekandcatch.feature.gameplay.navigation.navigateToFlashGame
import com.maxot.seekandcatch.feature.gameplay.navigation.navigateToFlowGame
import com.maxot.seekandcatch.feature.gameplay.navigation.navigateToGameResult
import com.maxot.seekandcatch.feature.gameplay.navigation.navigateToGameSelection
import com.maxot.seekandcatch.feature.leaderboard.navigation.LEADERBOARD_ROUTE
import com.maxot.seekandcatch.feature.leaderboard.navigation.leaderboardScreen
import com.maxot.seekandcatch.feature.leaderboard.navigation.navigateToLeaderboard

private enum class TopLevelDestination(
    val route: String,
    val label: String,
) {
    GAME(route = GAME_MAIN_ROUTE, label = "Game"),
    LEADERBOARD(route = LEADERBOARD_ROUTE, label = "Leaderboard"),
    ACCOUNT(route = ACCOUNT_ROUTE, label = "Account"),
}

@Composable
fun App() {
    SeekAndCatchTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val topLevelDestinations = TopLevelDestination.entries

        Scaffold(
            bottomBar = {
                NavigationBar {
                    topLevelDestinations.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                val navOptions = NavOptions.Builder()
                                    .setPopUpTo(
                                        navController.graph.findStartDestination().id,
                                        inclusive = false,
                                        saveState = true
                                    )
                                    .setLaunchSingleTop(true)
                                    .setRestoreState(true)
                                    .build()
                                when (destination) {
                                    TopLevelDestination.GAME -> navController.navigateToGameSelection(navOptions)
                                    TopLevelDestination.LEADERBOARD -> navController.navigateToLeaderboard(navOptions)
                                    TopLevelDestination.ACCOUNT -> navController.navigateToAccount(navOptions)
                                }
                            },
                            icon = {
                                when (destination) {
                                    TopLevelDestination.GAME -> Icon(Icons.Filled.PlayArrow, contentDescription = destination.label)
                                    TopLevelDestination.LEADERBOARD -> Icon(Icons.Filled.List, contentDescription = destination.label)
                                    TopLevelDestination.ACCOUNT -> Icon(Icons.Filled.AccountCircle, contentDescription = destination.label)
                                }
                            },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = GAME_MAIN_ROUTE,
                modifier = Modifier.padding(innerPadding)
            ) {
                gameSelectionScreen(
                    navigateToFlowGame = { navOptions -> navController.navigateToFlowGame(navOptions) },
                    navigateToFlashGame = { navOptions -> navController.navigateToFlashGame(navOptions) },
                    navigateToGameResult = { score -> navController.navigateToGameResult(score) },
                    navigateToGameSelection = { navOptions -> navController.navigateToGameSelection(navOptions) }
                )
                leaderboardScreen()
                accountScreen()
            }
        }
    }
}
