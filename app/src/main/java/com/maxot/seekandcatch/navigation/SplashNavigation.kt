package com.maxot.seekandcatch.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.maxot.seekandcatch.ui.SplashScreen

const val SPLASH_ROUTE = "splash"

fun NavGraphBuilder.splashScreen(
    isAppReady: () -> Boolean,
    onSplashComplete: () -> Unit,
) {
    composable(SPLASH_ROUTE) {
        SplashScreen(
            isAppReady = isAppReady,
            onSplashComplete = onSplashComplete,
        )
    }
}
