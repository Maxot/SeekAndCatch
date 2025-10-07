package com.maxot.seekandcatch.feature.leaderboard

import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.data.model.GameMode
import com.maxot.seekandcatch.data.model.LeaderboardRecord

sealed interface LeaderboardUiState {
    data class Successful(
        val data: List<LeaderboardRecord>,
        val selectedMode: GameMode?,
        val selectedDifficulty: GameDifficulty?,
    ) : LeaderboardUiState
    data object Loading : LeaderboardUiState
    data object Failed : LeaderboardUiState
}
