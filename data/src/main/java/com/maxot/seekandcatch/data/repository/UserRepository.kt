package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.data.model.User

interface UserRepository {
    suspend fun getUser(): User?
}