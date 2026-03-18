package com.maxot.seekandcatch.core.domain.flash

import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal

sealed class FlashGameState {
    data object Idle : FlashGameState()
    data class Created(val figuresSuitableForGoal: Set<Figure>) : FlashGameState()
    data object Started : FlashGameState()
    data class Resumed(val data: FlashGameData) : FlashGameState()
    data object Paused : FlashGameState()
    data class Finished(val score: Int, val lastData: FlashGameData? = null) : FlashGameState()
}

/**
 * Flash game data: a grid where some cells become visible for a short time.
 */
data class FlashGameData(
    val goals: Set<Goal<Any>> = emptySet(),
    val gridSize: Int = 16, // cells
    val visibleCells: Set<Int> = emptySet(), // indices of currently visible figures
    val figuresByCell: Map<Int, Figure> = emptyMap(), // what figure is shown in a cell when visible
    val goalSuitableFigures: Set<Figure> = emptySet(),
    val maxLifeCount: Int = 3,
    val lifeCount: Int = 0,
    val score: Int = 0,
    val coefficient: Float = 1f,
    val gameDuration: Long = 0L,
    val flashMillis: Long = 1200L, // how long a figure is visible
    val spawnPeriodMillis: Long = 900L // how often new figures appear
)
