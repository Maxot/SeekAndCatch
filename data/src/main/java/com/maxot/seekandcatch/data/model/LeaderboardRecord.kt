package com.maxot.seekandcatch.data.model

data class LeaderboardRecord(
    val userName: String? = null,
    val score: Int? = null,
    val gameMode: GameMode? = null,
    val difficulty: GameDifficulty? = null,
)
