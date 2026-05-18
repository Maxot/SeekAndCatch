package com.maxot.seekandcatch.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import com.maxot.seekandcatch.feature.account.navigation.ACCOUNT_ROUTE
import com.maxot.seekandcatch.feature.account.navigation.navigateToAccount
import com.maxot.seekandcatch.feature.gameplay.navigation.FLASH_GAME_ROUTE
import com.maxot.seekandcatch.feature.gameplay.navigation.FLOW_GAME_ROUTE
import com.maxot.seekandcatch.feature.gameplay.navigation.GAME_SELECTION_ROUTE
import com.maxot.seekandcatch.feature.gameplay.navigation.navigateToGameSelection
import com.maxot.seekandcatch.feature.leaderboard.navigation.LEADERBOARD_ROUTE
import com.maxot.seekandcatch.feature.leaderboard.navigation.navigateToLeaderboard
import com.maxot.seekandcatch.feature.settings.AudioController
import com.maxot.seekandcatch.navigation.TopLevelDestination
import kotlinx.coroutines.CoroutineScope

@Stable
class SeekAndCatchAppState(
    val navController: NavHostController,
    val coroutineScope: CoroutineScope,
    val audioController: AudioController
) {
    val currentDestination: NavDestination?
        @Composable get() = navController
            .currentBackStackEntryAsState().value?.destination

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() = when (currentDestination?.route) {
            LEADERBOARD_ROUTE -> TopLevelDestination.LEADERBOARD
            GAME_SELECTION_ROUTE -> TopLevelDestination.GAME
            ACCOUNT_ROUTE -> TopLevelDestination.ACCOUNT
            else -> null
        }
    val shouldShowBottomBar: Boolean
        @Composable get() = currentDestination.isTopLevelDestination()

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        audioController.playMenuMusic()
        val topLevelNavOptions = navOptions {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        when (topLevelDestination) {
            TopLevelDestination.LEADERBOARD -> navController.navigateToLeaderboard(topLevelNavOptions)
            TopLevelDestination.GAME -> navController.navigateToGameSelection(topLevelNavOptions)
            TopLevelDestination.ACCOUNT -> navController.navigateToAccount(topLevelNavOptions)
        }
    }

    @Composable
    fun ObserveMusicByDestination() {
        val destination = currentDestination
        androidx.compose.runtime.LaunchedEffect(destination) {
            when (destination?.route) {
                GAME_SELECTION_ROUTE, LEADERBOARD_ROUTE, ACCOUNT_ROUTE -> audioController.playMenuMusic()
            }
        }
    }
}

private fun getMusicTypeForRoute(route: String?): Boolean {
    return when (route) {
        GAME_SELECTION_ROUTE, LEADERBOARD_ROUTE, ACCOUNT_ROUTE -> true
        else -> false
    }
}

fun NavDestination?.isTopLevelDestination() =
    this?.hierarchy?.any { navDestination ->
        TopLevelDestination.entries.forEach { destination ->
            if (destination.route == navDestination.route) return@any true
        }
        false
    } ?: false
