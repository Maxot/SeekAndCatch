package com.maxot.seekandcatch.feature.leaderboard.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.maxot.seekandcatch.feature.leaderboard.ui.LeaderBoardScreen

const val LEADERBOARD_ROUTE = "leaderboard_route"

fun NavController.navigateToLeaderboard(navOptions: NavOptions) =
    navigate(LEADERBOARD_ROUTE, navOptions)

fun NavGraphBuilder.leaderboardScreen() {
    composable(route = LEADERBOARD_ROUTE,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(1000)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(1000)
            )
        }
    ) {
        LeaderBoardScreen()
    }
}