package com.maxot.seekandcatch.feature.account.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.maxot.seekandcatch.feature.account.ui.AccountScreenRoute

const val ACCOUNT_ROUTE = "account_route"

fun NavController.navigateToAccount(navOptions: NavOptions) = navigate(ACCOUNT_ROUTE, navOptions)

fun NavGraphBuilder.accountScreen() {
    composable(route = ACCOUNT_ROUTE) {
        AccountScreenRoute()
    }
}