package com.maxot.seekandcatch.data.repository


import com.maxot.seekandcatch.data.model.Goal
import javax.inject.Inject
import kotlin.random.Random

class GoalsRepositoryImpl
@Inject constructor() : GoalsRepository {
    override fun getRandomGoal(): Goal<Any> =
        Goal.getRandomGoal(Random.nextInt(4))
}

