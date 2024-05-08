package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.core.common.di.ApplicationScope
import com.maxot.seekandcatch.data.firebase.datasource.LeaderboardDataSource
import com.maxot.seekandcatch.data.model.LeaderboardRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class LeaderboardRepositoryImpl
@Inject constructor(
    @ApplicationScope private val externalScope: CoroutineScope,
    private val dataSource: LeaderboardDataSource,
    private val accountRepository: AccountRepository
) : LeaderboardRepository {
    override fun observeRecords(): Flow<List<LeaderboardRecord>> =
        dataSource.observeRecords()

    override suspend fun addRecord(record: LeaderboardRecord) {
        val userId = accountRepository.observeUserId().first()

        //TODO: Try to find better solution?
        dataSource.addRecord(record = record, userId) { documentId ->
            externalScope.launch {
                accountRepository.setUserId(documentId)
            }
        }
    }
}
