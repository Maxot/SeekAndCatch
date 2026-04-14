package com.maxot.seekandcatch.data.firebase.datasource

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import com.maxot.seekandcatch.core.common.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserFirestoreDataSource @Inject constructor() : UserDataSource {

    private val db = Firebase.firestore
    private val usersCollection = db.collection(COLLECTION_NAME_USERS)

    override suspend fun saveUser(user: User) {
        try {
            usersCollection.document(user.id).set(user).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user", e)
        }
    }

    override suspend fun getUser(userId: String): User? {
        return try {
            usersCollection.document(userId).get().await().toObject<User>()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user", e)
            null
        }
    }

    override fun observeUsers(): Flow<List<User>> =
        usersCollection.snapshots().map { querySnapshot ->
            querySnapshot.toObjects<User>()
        }

    companion object {
        private const val TAG = "UserFirestoreDataSource"
        private const val COLLECTION_NAME_USERS = "users"
    }
}
