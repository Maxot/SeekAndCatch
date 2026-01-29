package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.core.common.model.User

interface UserRepository {
    suspend fun getUser(): User?
}