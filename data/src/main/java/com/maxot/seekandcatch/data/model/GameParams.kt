package com.maxot.seekandcatch.data.model

/**
 * Represent the parameters that use to setup the game.
 * @param itemsCount count of items for the game.
 * @param percentOfSuitableItem percent of items that fit for the goal.
 * @param coefficientStep the step size that used to increase coefficient.
 * @param scorePoint the minimum amount of points that confer for right action.
 * @param rowDuration the amount ot time for one row of items to be scrolled by, in mils.
 * @param rowWidth the items count in one row.
 * @param maxLifeCount the maximal count of possible mistake.
 * @param lifeCount the default count of possible mistake
 * @param itemsPassedWithoutMissToGetLife the required number of items passed without a single miss to gain one more life.
 */
data class GameParams(
    val itemsCount: Int = 1000,
    val percentOfSuitableItem: Float = 0.25f,
    val coefficientStep: Float = 0.25f,
    val scorePoint: Int = 10,
    val rowDuration: Int = 1000,
    val rowWidth: Int = 4,
    val maxLifeCount: Int = 5,
    val lifeCount: Int = 3,
    val itemsPassedWithoutMissToGetLife: Int = 30
)