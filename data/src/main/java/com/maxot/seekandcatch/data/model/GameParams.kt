package com.maxot.seekandcatch.data.model

/**
 * Represent the parameters that use to setup the game.
 */
data class GameParams(
    val itemsCount: Int = 1000,
    val percentOfSuitableItem: Float = 0.25f,
    val coefficientStep: Float = 0.25f,
    val scorePoint: Int = 10
)