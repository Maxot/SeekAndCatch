package com.maxot.seekandcatch.core.common.model

data class LeaderboardRecord(
    val userId: String? = null,
    val userName: String? = null,
    val score: Int? = null,
    val gameMode: GameMode? = null,
    val difficulty: GameDifficulty? = null,
)
