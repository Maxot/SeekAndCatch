package com.maxot.seekandcatch.core.domain.engine

import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal

sealed class GameEngineState {
    data object Idle : GameEngineState()
    data class Created(val goalSuitableFigures: Set<Figure>) : GameEngineState()
    data object Started : GameEngineState()
    data object Paused : GameEngineState()
    data class Finished(val score: Int) : GameEngineState()
}

data class GameEngineData(
    val goals: Set<Goal<Any>> = emptySet(),
    val figures: List<Figure> = emptyList(),
    val goalSuitableFigures: Set<Figure> = emptySet(),
    val maxLifeCount: Int = 5,
    val lifeCount: Int = 0,
    val score: Int = 0,
    val coefficient: Float = 1f,
    val gameDuration: Long = 0,
    val scrollDuration: Int = 0,
    val pixelsToScroll: Float = 0f,
    val rowWidth: Int = 4,
    val visibleCells: Set<Int> = emptySet(),
    val flashMillis: Long = 1200L,
    val spawnPeriodMillis: Long = 900L,
)
