package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.data.firebase.datasource.auth.AuthDataSource
import javax.inject.Inject

class AuthRepositoryImpl
@Inject constructor(
    private val authDataSource: AuthDataSource,
) : AuthRepository {

    override suspend fun getUserId(): String {
        return try {
            val result = authDataSource.getOrCreateUser()?.uid
            result ?: throw IllegalStateException("Failed to get UID after anonymous sign in")
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun autoRegisterIfNeeded() {
        authDataSource.getOrCreateUser()
    }

}
