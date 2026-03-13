package com.maxot.seekandcatch.feature.gameplay.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.maxot.seekandcatch.core.common.model.GameMode
import com.maxot.seekandcatch.feature.gameplay.gameselection.GameSelectionScreen
import com.maxot.seekandcatch.feature.gameplay.ui.GameResultScreen
import com.maxot.seekandcatch.feature.gameplay.ui.flowgame.FlowGameScreen
import com.maxot.seekandcatch.feature.gameplay.ui.flashgame.FlashGameScreen

const val GAME_MAIN_ROUTE = "game_main_route"
const val GAME_SELECTION_ROUTE = "game_selection_route"
const val FLOW_GAME_ROUTE = "flow_game_route"
const val FLASH_GAME_ROUTE = "flash_game_route"
const val GAME_RESULT_ROUTE = "game_result_route/{score}"
const val SCORE_ARG = "score"

fun NavController.navigateToGameSelection(navOptions: NavOptions? = null) =
    navigate(GAME_SELECTION_ROUTE, navOptions)

fun NavController.navigateToFlowGame(navOptions: NavOptions? = null) =
    navigate(FLOW_GAME_ROUTE, navOptions)

fun NavController.navigateToFlashGame(navOptions: NavOptions? = null) =
    navigate(FLASH_GAME_ROUTE, navOptions)

fun NavController.navigateToGameResult(score: Int, navOptions: NavOptions? = null) =
    navigate("game_result_route/$score", navOptions)

fun NavGraphBuilder.gameSelectionScreen(
    navigateToFlowGame: (NavOptions?) -> Unit,
    navigateToFlashGame: (NavOptions?) -> Unit,
    navigateToGameResult: (Int) -> Unit,
    navigateToGameSelection: (NavOptions?) -> Unit
) {
    navigation(
        startDestination = GAME_SELECTION_ROUTE,
        route = GAME_MAIN_ROUTE
    ) {
        composable(
            route = GAME_SELECTION_ROUTE,
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
            GameSelectionScreen(
                navigateToFlowGame = navigateToFlowGame,
                navigateToFlashGame = navigateToFlashGame,
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
            FlowGameScreen(toGameResultScreen = navigateToGameResult)
        }
        composable(
            route = FLASH_GAME_ROUTE,
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
            FlashGameScreen(toGameResultScreen = navigateToGameResult)
        }
        composable(
            route = GAME_RESULT_ROUTE,
            arguments = listOf(
                navArgument(SCORE_ARG) { type = NavType.IntType }
            ),
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
            GameResultScreen(
                toMainScreen = { navigateToGameSelection(null) },
                onRestart = { mode ->
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(GAME_SELECTION_ROUTE, inclusive = false)
                        .build()
                    when (mode) {
                        GameMode.FLOW -> navigateToFlowGame(navOptions)
                        GameMode.FLASH -> navigateToFlashGame(navOptions)
                        else -> Unit
                    }
                }
            )
        }
    }
}
