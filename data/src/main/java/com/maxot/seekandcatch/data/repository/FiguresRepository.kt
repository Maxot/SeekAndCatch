package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal

interface FiguresRepository {
    fun getRandomFigure(id: Int): Figure

    fun getRandomFigures(itemsCount: Int): List<Figure>

    /**
     * Return list of random [Figure] with specific params
     *
     * @param itemsCount the count of items to return.
     * @param startId number from that start ids for [Figure].
     * @param percentageOfSuitableGoalItems the percent of items that fit for goal.
     * @param goal the [Goal] that used to determine if items fit for.
     *
     */
    fun getRandomFigures(
        itemsCount: Int,
        startId: Int = 0,
        percentageOfSuitableGoalItems: Float,
        goal: Goal<Any>
    ): List<Figure>
}