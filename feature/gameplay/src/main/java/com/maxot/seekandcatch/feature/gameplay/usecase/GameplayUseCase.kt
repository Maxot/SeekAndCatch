package com.maxot.seekandcatch.feature.gameplay.usecase


import com.maxot.seekandcatch.feature.gameplay.data.Figure
import com.maxot.seekandcatch.feature.gameplay.data.Goal
import javax.inject.Inject

@Deprecated("")
class GameplayUseCase
@Inject constructor() {

    fun checkGoalCondition(goals: Set<Goal<Any>>, figure: Figure): Boolean {
        var condition = false
        run goal@{
            goals.forEach { goal ->
                condition = when (goal) {
                    is Goal.Colored -> {
                        goal.getGoal() == figure.color
                    }

                    is Goal.Figured -> {
                        if (goal.getGoal().color == null) {
                            goal.getGoal().type == figure.type
                        } else {
                            goal.getGoal() == figure
                        }
                    }
                }
                if (condition) return@goal
            }
        }
        return condition
    }

    fun calculateCountOfSuitableFigures(figures: List<Figure>, goals: Set<Goal<Any>>): Int {
        var count = 0
        figures.forEach { figure ->
            if (checkGoalCondition(goals = goals, figure))
                count++
        }
        return count
    }

    fun calculateFrameDuration(
        baseDuration: Long = 1000L,
        oneFigureDuration: Long = 200L,
        delayCoefficient: Float = 1f,
        frameFigures: List<Figure>,
        goals: Set<Goal<Any>>
    ): Long {
        val countOfSuitableFigures =
            calculateCountOfSuitableFigures(figures = frameFigures, goals = goals)
        return (baseDuration + countOfSuitableFigures * oneFigureDuration * delayCoefficient).toLong()
    }

    fun calculateLevelDuration(baseLevelDuration: Long, itemsCount: Int): Long {
        val levelDuration = baseLevelDuration + itemsCount * 80

        return levelDuration
    }
}