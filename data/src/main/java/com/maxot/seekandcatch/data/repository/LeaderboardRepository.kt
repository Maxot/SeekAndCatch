package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.data.model.LeaderboardRecord
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    fun observeRecords(): Flow<List<LeaderboardRecord>>

    fun addRecord(record: LeaderboardRecord)
}
