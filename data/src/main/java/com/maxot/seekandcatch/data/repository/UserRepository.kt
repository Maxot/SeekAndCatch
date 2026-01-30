package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.core.common.model.User

interface UserRepository {
    suspend fun getUser(userId: String): User?
    suspend fun saveUser(user: User)
}