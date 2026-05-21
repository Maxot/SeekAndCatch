package com.maxot.seekandcatch.data.repository

import android.util.Log
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.maxot.seekandcatch.core.common.model.User
import com.maxot.seekandcatch.data.firebase.datasource.auth.FirebaseAuthDataSource
import javax.inject.Inject

private const val TAG = "AuthRepositoryImpl"

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
            Firebase.crashlytics.recordException(e)
            Log.w(TAG, "getUserId failed", e)
            ""
        }
    }

    override suspend fun signOut() {
        firebaseAuthDataSource.signOut()
    }

    override suspend fun deleteAccount(): Boolean {
        return firebaseAuthDataSource.deleteAccount()
    }

}
