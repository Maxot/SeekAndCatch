package com.maxot.seekandcatch.data.firebase.datasource

import com.maxot.seekandcatch.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserDataSource {
    suspend fun saveUser(user: User)
    suspend fun getUser(userId: String): User?
    fun observeUsers(): Flow<List<User>>
}
