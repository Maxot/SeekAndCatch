package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.core.common.model.User

interface AuthRepository {

    suspend fun getOrCreateUser(): User?

    suspend fun getUserId(): String

}
