package com.maxot.seekandcatch.feature.gameplay.data.repository

import com.maxot.seekandcatch.feature.gameplay.data.Figure
import com.maxot.seekandcatch.feature.gameplay.data.Goal

interface FiguresRepository {
    fun getRandomFigure(): Figure

    fun getRandomFigures(itemsCount: Int): List<Figure>

    /**
     * Return list of random [Figure] with specific params
     *
     * @param itemsCount the count of items to return.
     * @param percentOfGoalSuitedItems the percent of items that fit for goal.
     * @param goal the [Goal] that used to determine if items fit for.
     *
     */
    fun getRandomFigures(
        itemsCount: Int,
        percentOfGoalSuitedItems: Float,
        goal: Goal<Any>
    ): List<Figure>
}