package com.maxot.seekandcatch.feature.gameplay.data.repository

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.feature.gameplay.data.Goal
import com.maxot.seekandcatch.feature.gameplay.data.Goal.Companion.getRandomGoal
import com.maxot.seekandcatch.feature.gameplay.getRandomNumber
import javax.inject.Inject

class GoalsRepositoryImpl
@Inject constructor() : GoalsRepository {
    override fun getGoal(): Goal<Any> {
        TODO("Not yet implemented")
    }

    override fun getRandomGoals(seed: Int): Set<Goal<Any>> {
        return when (seed) {
            0 -> setOf(getRandomGoal(Int.getRandomNumber()))
            1 -> setOf(getRandomGoal(Int.getRandomNumber()), getRandomGoal(Int.getRandomNumber()))
            else -> setOf(Goal.Colored(Color.Red))
        }
    }

}
