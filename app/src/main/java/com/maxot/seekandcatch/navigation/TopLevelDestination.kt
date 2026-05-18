package com.maxot.seekandcatch.navigation

import com.maxot.seekandcatch.core.designsystem.icon.SaCIcons
import com.maxot.seekandcatch.feature.account.navigation.ACCOUNT_ROUTE
import com.maxot.seekandcatch.feature.gameplay.navigation.GAME_SELECTION_ROUTE
import com.maxot.seekandcatch.feature.leaderboard.navigation.LEADERBOARD_ROUTE
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import com.maxot.seekandcatch.feature.gameplay.generated.resources.Res as GameplayRes
import com.maxot.seekandcatch.feature.gameplay.generated.resources.feature_gameplay_title
import com.maxot.seekandcatch.feature.leaderboard.generated.resources.Res as LeaderboardRes
import com.maxot.seekandcatch.feature.leaderboard.generated.resources.feature_leaderboard_title
import com.maxot.seekandcatch.feature.account.generated.resources.Res as AccountRes
import com.maxot.seekandcatch.feature.account.generated.resources.feature_account_title

enum class TopLevelDestination(
    val selectedIcon: DrawableResource,
    val unselectedIcon: DrawableResource,
    val iconTextId: StringResource,
    val titleTextId: StringResource,
    val route: String
) {
    LEADERBOARD(
        selectedIcon = SaCIcons.Leaderboard,
        unselectedIcon = SaCIcons.Leaderboard,
        iconTextId = LeaderboardRes.string.feature_leaderboard_title,
        titleTextId = LeaderboardRes.string.feature_leaderboard_title,
        route = LEADERBOARD_ROUTE
    ),
    GAME(
        selectedIcon = SaCIcons.Play,
        unselectedIcon = SaCIcons.Play,
        iconTextId = GameplayRes.string.feature_gameplay_title,
        titleTextId = GameplayRes.string.feature_gameplay_title,
        route = GAME_SELECTION_ROUTE
    ),
    ACCOUNT(
        selectedIcon = SaCIcons.Account,
        unselectedIcon = SaCIcons.Account,
        iconTextId = AccountRes.string.feature_account_title,
        titleTextId = AccountRes.string.feature_account_title,
        route = ACCOUNT_ROUTE
    ),
}
