package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.core.common.model.Goal

interface GoalsRepository {
    suspend fun getRandomGoal(): Goal<Any>
}
