package com.maxot.seekandcatch.feature.gameplay

data class GameResultUiState(
    val userName: String = "",
    val lastScore: Int = 0,
    // Best score found in remote leaderboard for the same user, game mode and difficulty
    val remoteBestForContext: Int = 0,
    val showUserNameDialog: Boolean = false,
    val isProcessing: Boolean = false,
) {
    // New best is determined against remote leaderboard for current mode & difficulty
    val isNewBest: Boolean get() = lastScore > remoteBestForContext
}
