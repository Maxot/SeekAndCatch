package com.maxot.seekandcatch.feature.leaderboard.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.maxot.seekandcatch.feature.leaderboard.ui.LeaderBoardScreenRoute

const val LEADERBOARD_ROUTE = "leaderboard_route"

fun NavController.navigateToLeaderboard(navOptions: NavOptions) = navigate(LEADERBOARD_ROUTE, navOptions)

fun NavGraphBuilder.leaderboardScreen() {
    composable(route = LEADERBOARD_ROUTE) {
        LeaderBoardScreenRoute()
    }
}