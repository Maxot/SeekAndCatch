package com.maxot.seekandcatch.feature.gameplay

import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode

data class GameResultUiState(
    val userName: String = "",
    val lastScore: Int = 0,
    // Best score found in remote leaderboard for the same user, game mode and difficulty
    val remoteBestForContext: Int = 0,
    val isProcessing: Boolean = false,
    val gameMode: GameMode? = null,
    val gameDifficulty: GameDifficulty? = null,
    val hasPlayedNewBestSound: Boolean = false,
    val isNewBest: Boolean = false,
)
