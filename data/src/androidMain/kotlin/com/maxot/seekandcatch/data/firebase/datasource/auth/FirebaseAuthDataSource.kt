package com.maxot.seekandcatch.data.firebase.datasource.auth

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseAuthDataSource() {
    private val auth = Firebase.auth

    suspend fun getOrCreateUser(): FirebaseUser? {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            return currentUser
        }

        return try {
            val result = auth.signInAnonymously().await()
            result.user ?: throw IllegalStateException("Failed to get User after anonymous sign in")
        } catch (e: Exception) {
            null
        }
    }
}
