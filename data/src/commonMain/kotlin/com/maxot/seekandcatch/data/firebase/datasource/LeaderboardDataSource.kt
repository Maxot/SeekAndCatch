package com.maxot.seekandcatch.data.firebase.datasource

import com.maxot.seekandcatch.core.common.model.LeaderboardRecord
import kotlinx.coroutines.flow.Flow

interface LeaderboardDataSource {
    fun observeRecords(): Flow<List<LeaderboardRecord>>

    fun addRecord(record: LeaderboardRecord, userId: String, onSuccessful: (String) -> Unit)
}