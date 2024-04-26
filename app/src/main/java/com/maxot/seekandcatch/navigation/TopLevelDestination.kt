package com.maxot.seekandcatch.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.maxot.seekandcatch.core.designsystem.icon.SaCIcons
import com.maxot.seekandcatch.feature.gameplay.navigation.GAME_SELECTION_ROUTE
import com.maxot.seekandcatch.feature.leaderboard.navigation.LEADERBOARD_ROUTE
import com.maxot.seekandcatch.feature.settings.navigation.SETTINGS_ROUTE
import com.maxot.seekandcatch.feature.gameplay.R as gameplayR
import com.maxot.seekandcatch.feature.leaderboard.R as leaderboardR
import com.maxot.seekandcatch.feature.settings.R as settingsR

/**
 * Type for the top level destinations in the application. Each of these destinations
 * can contain one or more screens (based on the window size). Navigation from one screen to the
 * next within a single destination will be handled directly in composables.
 */
enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int,
    val titleTextId: Int,
    val route: String
) {
    LEADERBOARD(
        selectedIcon = SaCIcons.Leaderboard,
        unselectedIcon = SaCIcons.UnselectedLeaderboard,
        iconTextId = leaderboardR.string.feature_leaderboard_title,
        titleTextId = leaderboardR.string.feature_leaderboard_title,
        route = LEADERBOARD_ROUTE
    ),
    GAME(
        selectedIcon = SaCIcons.Play,
        unselectedIcon = SaCIcons.UnselectedPlay,
        iconTextId = gameplayR.string.feature_gameplay_title,
        titleTextId = gameplayR.string.feature_gameplay_title,
        route = GAME_SELECTION_ROUTE
    ),
    SETTINGS(
        selectedIcon = SaCIcons.Settings,
        unselectedIcon = SaCIcons.UnselectedSettings,
        iconTextId = settingsR.string.feature_settings_title,
        titleTextId = settingsR.string.feature_settings_title,
        route = SETTINGS_ROUTE
    ),
}
