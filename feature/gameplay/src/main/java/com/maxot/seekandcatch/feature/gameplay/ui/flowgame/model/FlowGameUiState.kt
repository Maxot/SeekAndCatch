package com.maxot.seekandcatch.feature.gameplay.ui.flowgame.model

import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal

data class FlowGameUiState(
    val goals: Set<Goal<Any>> = emptySet(),
    val figures: List<Figure> = emptyList(),
    val goalSuitableFigures: Set<Figure> = emptySet(),
    val maxLifeCount: Int = 5,
    val lifeCount: Int = 0,
    val score: Int = 0,
    val coefficient: Float = 0f,
    val gameDuration: Long = 0,
    val scrollDuration: Int = 0,
    val pixelsToScroll: Float = 0f,
    val rowWidth: Int = 4,
    val isLoading: Boolean = false,
    val isReady: Boolean = false,
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val isLifeWasted: Boolean = false
)