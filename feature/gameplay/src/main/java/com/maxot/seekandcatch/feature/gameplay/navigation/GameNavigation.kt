package com.maxot.seekandcatch.feature.gameplay.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.maxot.seekandcatch.feature.gameplay.ui.FlowGameScreenRoute
import com.maxot.seekandcatch.feature.gameplay.ui.GameResultScreen
import com.maxot.seekandcatch.feature.gameplay.gameselection.GameSelectionScreenRoute

const val GAME_MAIN_ROUTE = "game_main_route"
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
    navigation(
        startDestination = GAME_SELECTION_ROUTE,
        route = GAME_MAIN_ROUTE
    ) {
        composable(route = GAME_SELECTION_ROUTE,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    tween(1000)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(1000)
                )
            }
        ) {
            GameSelectionScreenRoute(
                navigateToFlowGame = navigateToFlowGame
            )
        }
        composable(
            route = FLOW_GAME_ROUTE,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(1000)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(1000)
                )
            },
        ) {
            FlowGameScreenRoute(toGameResultScreen = navigateToGameResult)
        }
        composable(route = GAME_RESULT_ROUTE,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(1000)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    tween(1000)
                )
            }) {
            GameResultScreen(toMainScreen = navigateToGameSelection)
        }
    }
}
