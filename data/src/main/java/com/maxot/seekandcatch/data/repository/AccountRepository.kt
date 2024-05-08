package com.maxot.seekandcatch.data.repository

import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    suspend fun setUserName(name: String)

    fun observeUserName(): Flow<String>

    suspend fun setUserId(id: String)

    fun observeUserId(): Flow<String>
}