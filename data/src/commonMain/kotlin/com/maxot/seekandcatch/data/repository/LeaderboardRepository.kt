package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.core.common.model.LeaderboardRecord
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    fun observeRecords(): Flow<List<LeaderboardRecord>>

    suspend fun addRecord(record: LeaderboardRecord)
}
