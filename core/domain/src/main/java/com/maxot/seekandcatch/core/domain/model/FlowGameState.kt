package com.maxot.seekandcatch.core.domain.model

import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal

sealed class FlowGameState {
    data object Idle:  FlowGameState()
    data object Created : FlowGameState()
    data object Started:  FlowGameState()
    data class Resumed(val data: FlowGameData):  FlowGameState()
    data object Paused:  FlowGameState()
    data class Finished(val score: Int):  FlowGameState()
}

data class FlowGameData(
    val goals: Set<Goal<Any>> = emptySet(),
    val figures: List<Figure> = emptyList(),
    val maxLifeCount: Int = 5,
    val lifeCount: Int = 0,
    val score: Int = 0,
    val coefficient: Float = 0f,
    val gameDuration: Long = 0,
    val scrollDuration: Int = 0,
    val pixelsToScroll: Float = 0f,
    val rowWidth: Int = 4,
)
