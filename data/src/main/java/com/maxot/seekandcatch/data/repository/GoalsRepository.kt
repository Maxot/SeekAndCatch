package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.data.model.Goal

interface GoalsRepository {
    fun getRandomGoal(): Goal<Any>
}
