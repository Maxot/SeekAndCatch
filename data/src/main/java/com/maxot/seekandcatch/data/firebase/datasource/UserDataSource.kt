package com.maxot.seekandcatch.data.firebase.datasource

import com.maxot.seekandcatch.core.common.model.User
import kotlinx.coroutines.flow.Flow

interface UserDataSource {
    suspend fun saveUser(user: User)
    suspend fun getUser(userId: String): User?
    suspend fun deleteUser(userId: String)
    fun observeUsers(): Flow<List<User>>
}
