package com.maxot.seekandcatch.data.repository


import com.maxot.seekandcatch.data.model.Goal
import javax.inject.Inject

class GoalsRepositoryImpl
@Inject constructor() : GoalsRepository {
    override fun getRandomGoal(): Goal<Any> =
        Goal.getRandomGoal()
}

