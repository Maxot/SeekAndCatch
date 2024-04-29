package com.maxot.seekandcatch.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import com.maxot.seekandcatch.feature.gameplay.navigation.GAME_SELECTION_ROUTE
import com.maxot.seekandcatch.feature.gameplay.navigation.navigateToGameSelection
import com.maxot.seekandcatch.feature.leaderboard.navigation.LEADERBOARD_ROUTE
import com.maxot.seekandcatch.feature.leaderboard.navigation.navigateToLeaderboard
import com.maxot.seekandcatch.feature.settings.navigation.SETTINGS_ROUTE
import com.maxot.seekandcatch.feature.settings.navigation.navigateToSettings
import com.maxot.seekandcatch.navigation.TopLevelDestination
import kotlinx.coroutines.CoroutineScope

@Stable
class SeekAndCatchAppState(
    val navController: NavHostController,
    val coroutineScope: CoroutineScope
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() = when (currentDestination?.route) {
            LEADERBOARD_ROUTE -> TopLevelDestination.LEADERBOARD
            GAME_SELECTION_ROUTE -> TopLevelDestination.GAME
            SETTINGS_ROUTE -> TopLevelDestination.SETTINGS
            else -> null
        }
    val shouldShowBottomBar: Boolean
        @Composable get() = currentDestination.isTopLevelDestination()

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        val topLevelNavOptions = navOptions {
        }
        when (topLevelDestination) {
            TopLevelDestination.LEADERBOARD -> navController.navigateToLeaderboard(
                topLevelNavOptions
            )

            TopLevelDestination.GAME -> navController.navigateToGameSelection(topLevelNavOptions)
            TopLevelDestination.SETTINGS -> navController.navigateToSettings(topLevelNavOptions)
        }

    }
}

fun NavDestination?.isTopLevelDestination() =
    this?.hierarchy?.any { navDestination ->
        TopLevelDestination.entries.forEach { destination ->
            if (destination.route == navDestination.route) return@any true
        }
        false
    } ?: false