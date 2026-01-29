package com.maxot.seekandcatch.feature.leaderboard

import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode

sealed interface LeaderboardEvent {
    data class SelectMode(val mode: GameMode?) : LeaderboardEvent
    data class SelectDifficulty(val difficulty: GameDifficulty?) : LeaderboardEvent
}
