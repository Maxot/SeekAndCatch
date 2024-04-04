package com.maxot.seekandcatch.feature.gameplay.data.repository

import com.maxot.seekandcatch.feature.gameplay.data.Goal
import com.maxot.seekandcatch.feature.gameplay.getRandomNumber
import javax.inject.Inject

class GoalsRepositoryImpl
@Inject constructor() : GoalsRepository {
    override fun getRandomGoal(): Goal<Any> =
        Goal.getRandomGoal(Int.getRandomNumber())
}

