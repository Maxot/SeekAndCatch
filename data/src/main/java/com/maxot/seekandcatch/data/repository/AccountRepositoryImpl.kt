package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.data.datastore.AccountDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AccountRepositoryImpl
@Inject constructor(
    private val accountDataStore: AccountDataStore
) : AccountRepository {

    override suspend fun setUserName(name: String) = accountDataStore.setUserName(name)

    override fun observeUserName(): Flow<String> = accountDataStore.userNameFlow
}