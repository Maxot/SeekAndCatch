package com.maxot.seekandcatch.data.repository

interface AuthRepository {

    suspend fun autoRegisterIfNeeded()

    suspend fun getUserId(): String

}
