package com.maxot.seekandcatch.data.test.repository

import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.data.repository.GoalsRepository

class FakeGoalsRepository : GoalsRepository {

    private var randomGoal: Goal<Any> = Goal.Shaped(Figure.FigureType.CIRCLE)
    override fun getRandomGoal(): Goal<Any> = randomGoal

    /**
     * A test-only API to allow controlling the goal from tests.
     */
    fun setRandomGoal(goal: Goal<Any>) {
        randomGoal = goal
    }
}