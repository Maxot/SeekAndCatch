package com.maxot.seekandcatch.feature.score.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.maxot.seekandcatch.feature.score.ui.ScoreScreen

const val SCORE_ROUTE = "score_route"

fun NavController.navigateToScore(navOptions: NavOptions? = null) = navigate(SCORE_ROUTE, navOptions)

fun NavGraphBuilder.scoreScreen(toGameSelection: () -> Unit) {
    composable(route = SCORE_ROUTE) {
        ScoreScreen(
            toMainScreen = toGameSelection
        )
    }
}