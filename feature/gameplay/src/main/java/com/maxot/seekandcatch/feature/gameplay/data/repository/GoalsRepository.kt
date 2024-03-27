package com.maxot.seekandcatch.feature.gameplay.data.repository

import com.maxot.seekandcatch.feature.gameplay.data.Goal

interface GoalsRepository {
    fun getGoal(): Goal<Any>
    fun getRandomGoals(seed: Int): Set<Goal<Any>>
}