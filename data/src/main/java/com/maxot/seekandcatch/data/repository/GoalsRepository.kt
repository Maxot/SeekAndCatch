package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.data.model.Goal

interface GoalsRepository {
    suspend fun getRandomGoal(): Goal<Any>
}
