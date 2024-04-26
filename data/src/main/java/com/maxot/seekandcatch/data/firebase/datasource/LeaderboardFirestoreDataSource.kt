package com.maxot.seekandcatch.data.firebase.datasource

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import com.maxot.seekandcatch.data.model.LeaderboardRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

const val TAG = "LeaderboardFirestoreDataSource"

class LeaderboardFirestoreDataSource
@Inject constructor() : LeaderboardDataSource {

    private val db = Firebase.firestore

    private val leaderboardCollection = db.collection(COLLECTION_NAME_LEADERBOARD)

    override fun observeRecords(): Flow<List<LeaderboardRecord>> =
        leaderboardCollection.snapshots().map { querySnapshot ->
            querySnapshot.toObjects<LeaderboardRecord>()
        }

    override fun addRecord(record: LeaderboardRecord) {
        val recordMap = hashMapOf(
            LEADERBOARD_DOCUMENT_USER_NAME_KEY to record.userName,
            LEADERBOARD_DOCUMENT_SCORE_KEY to record.score
        )

        leaderboardCollection
            .add(recordMap)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    companion object {
        const val COLLECTION_NAME_LEADERBOARD = "leaderboard"

        const val LEADERBOARD_DOCUMENT_USER_NAME_KEY = "userName"
        const val LEADERBOARD_DOCUMENT_SCORE_KEY = "score"
    }
}