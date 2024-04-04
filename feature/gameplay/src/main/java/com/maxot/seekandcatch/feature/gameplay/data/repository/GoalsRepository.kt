package com.maxot.seekandcatch.feature.gameplay.data.repository

import com.maxot.seekandcatch.feature.gameplay.data.Goal

interface GoalsRepository {
    fun getRandomGoal(): Goal<Any>
}
