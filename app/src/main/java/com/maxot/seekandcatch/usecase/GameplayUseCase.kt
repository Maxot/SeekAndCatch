package com.maxot.seekandcatch.usecase

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.data.Figure
import com.maxot.seekandcatch.data.Goal
import javax.inject.Inject

class GameplayUseCase
@Inject constructor(){

    fun checkGoalCondition(goals: List<Goal<Any>>, figure: Figure): Boolean {
        var condition = false
        run goal@{
            goals.forEach { goal ->
                condition = when (goal) {
                    is Goal.Colored -> {
                        goal.getGoal() == figure.color
                    }
                    is Goal.Figured -> {
                        if (goal.getGoal().color == Color.White){
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
}