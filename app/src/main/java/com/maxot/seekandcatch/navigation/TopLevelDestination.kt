package com.maxot.seekandcatch.navigation

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.maxot.seekandcatch.core.designsystem.R
import com.maxot.seekandcatch.core.designsystem.icon.SaCIcons
import com.maxot.seekandcatch.feature.account.navigation.ACCOUNT_ROUTE
import com.maxot.seekandcatch.feature.gameplay.navigation.GAME_SELECTION_ROUTE
import com.maxot.seekandcatch.feature.leaderboard.navigation.LEADERBOARD_ROUTE
import com.maxot.seekandcatch.feature.gameplay.R as gameplayR
import com.maxot.seekandcatch.feature.leaderboard.R as leaderboardR
import com.maxot.seekandcatch.feature.account.R as accountR

/**
 * Type for the top level destinations in the application. Each of these destinations
 * can contain one or more screens (based on the window size). Navigation from one screen to the
 * next within a single destination will be handled directly in composables.
 */
enum class TopLevelDestination(
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,
    val iconTextId: Int,
    val titleTextId: Int,
    val route: String
) {
    LEADERBOARD(
        selectedIcon = R.drawable.ic_leaderboard,
        unselectedIcon =  R.drawable.ic_leaderboard,
        iconTextId = leaderboardR.string.feature_leaderboard_title,
        titleTextId = leaderboardR.string.feature_leaderboard_title,
        route = LEADERBOARD_ROUTE
    ),
    GAME(
        selectedIcon = R.drawable.ic_game,
        unselectedIcon = R.drawable.ic_game,
        iconTextId = gameplayR.string.feature_gameplay_title,
        titleTextId = gameplayR.string.feature_gameplay_title,
        route = GAME_SELECTION_ROUTE
    ),
    ACCOUNT(
        selectedIcon = R.drawable.ic_account,
        unselectedIcon = R.drawable.ic_account,
        iconTextId = accountR.string.feature_account_title,
        titleTextId = accountR.string.feature_account_title,
        route = ACCOUNT_ROUTE
    ),
}
