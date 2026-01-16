package com.maxot.seekandcatch.feature.gameplay.ui.flashgame.model

import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal

data class FlashGameUiState(
    val isLoading: Boolean = true,
    val isReady: Boolean = false,
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val gridSize: Int = 16,
    val gridWidth: Int = 4,
    val visibleCells: Set<Int> = emptySet(),
    val score: Int = 0,
    val lifeCount: Int = 0,
    val gameDuration: Long = 0L,
    val coefficient: Float = 1f,
    val goals: Set<Goal<Any>> = emptySet(),
    val goalSuitableFigures: Set<Figure> = emptySet(),
    val figuresByCell: Map<Int, Figure> = emptyMap(),
    val isLifeWasted: Boolean = false
)
