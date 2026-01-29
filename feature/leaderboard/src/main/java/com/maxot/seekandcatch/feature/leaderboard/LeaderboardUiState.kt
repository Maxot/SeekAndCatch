package com.maxot.seekandcatch.feature.leaderboard

import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode
import com.maxot.seekandcatch.core.common.model.LeaderboardRecord

sealed interface LeaderboardUiState {
    data class Successful(
        val data: List<LeaderboardRecord>,
        val selectedMode: GameMode?,
        val selectedDifficulty: GameDifficulty?,
    ) : LeaderboardUiState
    data object Loading : LeaderboardUiState
    data object Failed : LeaderboardUiState
}
