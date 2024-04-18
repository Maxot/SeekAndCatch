package com.maxot.seekandcatch.data.model

/**
 * Represent the parameters that use to setup the game.
 * @param itemsCount count of items for the game.
 * @param percentOfSuitableItem percent of items that fit for the goal.
 * @param coefficientStep the step size that used to increase coefficient.
 * @param scorePoint the minimum amount of points that confer for right action.
 * @param itemDuration the amount ot time for one item, in mils.
 */
data class GameParams(
    val itemsCount: Int = 1000,
    val percentOfSuitableItem: Float = 0.25f,
    val coefficientStep: Float = 0.25f,
    val scorePoint: Int = 10,
    val itemDuration: Int = 50
)