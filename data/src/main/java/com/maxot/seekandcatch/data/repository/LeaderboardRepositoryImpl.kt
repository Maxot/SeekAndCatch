package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.core.common.di.ApplicationScope
import com.maxot.seekandcatch.data.firebase.datasource.LeaderboardDataSource
import com.maxot.seekandcatch.data.firebase.datasource.UserDataSource
import com.maxot.seekandcatch.core.common.model.LeaderboardRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class LeaderboardRepositoryImpl
@Inject constructor(
    @ApplicationScope private val externalScope: CoroutineScope,
    private val leaderboardDataSource: LeaderboardDataSource,
    private val userDataSource: UserDataSource,
    private val accountRepository: AuthRepository
) : LeaderboardRepository {
    override fun observeRecords(): Flow<List<LeaderboardRecord>> =
        combine(
            leaderboardDataSource.observeRecords(),
            userDataSource.observeUsers()
        ) { records, users ->
            val userMap = users.associateBy { it.id }
            records.map { record ->
                val userName = record.userId?.let { userMap[it]?.name } ?: record.userName
                record.copy(userName = userName)
            }
        }

    override suspend fun addRecord(record: LeaderboardRecord) {
        val userId = accountRepository.getUserId()

        leaderboardDataSource.addRecord(record = record, userId) { documentId ->

        }
    }
}
