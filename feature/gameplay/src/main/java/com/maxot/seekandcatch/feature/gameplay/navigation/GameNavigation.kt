package com.maxot.seekandcatch.feature.gameplay.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.maxot.seekandcatch.feature.gameplay.ui.FlowGameScreenRoute
import com.maxot.seekandcatch.feature.gameplay.ui.GameResultScreen
import com.maxot.seekandcatch.feature.gameplay.ui.GameSelectionScreenRoute

const val GAME_SELECTION_ROUTE = "game_selection_route"
const val FLOW_GAME_ROUTE = "flow_game_route"
const val GAME_RESULT_ROUTE = "game_result_route"

fun NavController.navigateToGameSelection(navOptions: NavOptions? = null) =
    navigate(GAME_SELECTION_ROUTE, navOptions)

fun NavController.navigateToFlowGame(navOptions: NavOptions? = null) =
    navigate(FLOW_GAME_ROUTE, navOptions)

fun NavController.navigateToGameResult(navOptions: NavOptions? = null) =
    navigate(GAME_RESULT_ROUTE, navOptions)

fun NavGraphBuilder.gameSelectionScreen(
    navigateToFlowGame: () -> Unit,
    navigateToGameResult: () -> Unit,
    navigateToGameSelection: () -> Unit
) {
    composable(route = GAME_SELECTION_ROUTE) {
        GameSelectionScreenRoute(
            navigateToFlowGame = navigateToFlowGame
        )
    }
    composable(route = FLOW_GAME_ROUTE) {
        FlowGameScreenRoute(toGameResultScreen = navigateToGameResult)
    }
    composable(route = GAME_RESULT_ROUTE) {
        GameResultScreen(toMainScreen = navigateToGameSelection)
    }
}