package com.maxot.seekandcatch.feature.account.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.maxot.seekandcatch.feature.account.ui.AccountScreen

const val ACCOUNT_ROUTE = "account_route"

fun NavController.navigateToAccount(navOptions: NavOptions) = navigate(ACCOUNT_ROUTE, navOptions)

fun NavGraphBuilder.accountScreen() {
    composable(
        route = ACCOUNT_ROUTE,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(1000)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(1000)
            )
        }) {
        AccountScreen()
    }
}