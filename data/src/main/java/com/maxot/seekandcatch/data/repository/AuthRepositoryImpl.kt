package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.core.common.model.User
import com.maxot.seekandcatch.data.firebase.datasource.auth.FirebaseAuthDataSource
import javax.inject.Inject

class AuthRepositoryImpl
@Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource
) : AuthRepository {

    override suspend fun getOrCreateUser(): User? {
        val user = firebaseAuthDataSource.getOrCreateUser()
        return user?.uid?.let {
            User(
                id = it,
                name = user.displayName ?: ""
            )
        }
    }

    override suspend fun getUserId(): String {
        return try {
            val result = firebaseAuthDataSource.getOrCreateUser()?.uid
            result ?: throw IllegalStateException("Failed to get UID after anonymous sign in")
        } catch (e: Exception) {
            ""
        }
    }

}
