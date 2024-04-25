package com.maxot.seekandcatch.feature.gameplay.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.maxot.seekandcatch.feature.gameplay.ui.FlowGameScreenRoute
import com.maxot.seekandcatch.feature.gameplay.ui.GameSelectionScreenRoute
import com.maxot.seekandcatch.feature.score.navigation.scoreScreen

const val GAME_SELECTION_ROUTE = "game_selection_route"
const val FLOW_GAME_ROUTE = "flow_game_route"

fun NavController.navigateToGameSelection(navOptions: NavOptions? = null) =
    navigate(GAME_SELECTION_ROUTE, navOptions)

fun NavController.navigateToFlowGame(navOptions: NavOptions? = null) =
    navigate(FLOW_GAME_ROUTE, navOptions)

fun NavGraphBuilder.gameSelectionScreen(
    navigateToFlowGame: () -> Unit,
    navigateToScore: () -> Unit,
    navigateToGameSelection: () -> Unit
) {
    composable(route = GAME_SELECTION_ROUTE) {
        GameSelectionScreenRoute(
            navigateToFlowGame = navigateToFlowGame
        )
    }
    composable(route = FLOW_GAME_ROUTE) {
        FlowGameScreenRoute(toScoreScreen = navigateToScore)
    }
    scoreScreen(
        toGameSelection = navigateToGameSelection
    )
}