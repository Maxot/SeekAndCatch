package com.maxot.seekandcatch.feature.gameplay

data class GameResultUiState(
    val userName: String = "",
    val lastScore: Int = 0,
    val bestScore: Int = 0,
    val showUserNameDialog: Boolean = false,
    val isProcessing: Boolean = false,
) {
    val isNewBest: Boolean get() = lastScore > bestScore
}
