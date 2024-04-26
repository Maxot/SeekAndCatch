package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.data.firebase.datasource.LeaderboardDataSource
import com.maxot.seekandcatch.data.model.LeaderboardRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LeaderboardRepositoryImpl
@Inject constructor(
    private val dataSource: LeaderboardDataSource
) : LeaderboardRepository {
    override fun observeRecords(): Flow<List<LeaderboardRecord>> =
        dataSource.observeRecords()

    override fun addRecord(record: LeaderboardRecord) = dataSource.addRecord(record = record)
}
