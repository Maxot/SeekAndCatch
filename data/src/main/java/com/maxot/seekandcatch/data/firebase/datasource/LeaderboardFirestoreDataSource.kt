package com.maxot.seekandcatch.data.firebase.datasource

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import com.maxot.seekandcatch.core.common.model.LeaderboardRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val TAG = "LeaderboardFirestoreDataSource"

class LeaderboardFirestoreDataSource
@Inject constructor() : LeaderboardDataSource {

    private val db = Firebase.firestore

    private val leaderboardCollection = db.collection(COLLECTION_NAME_LEADERBOARD)

    override fun observeRecords(): Flow<List<LeaderboardRecord>> =
        leaderboardCollection.snapshots().map { querySnapshot ->
            querySnapshot.toObjects<LeaderboardRecord>()
        }

    override fun addRecord(record: LeaderboardRecord, userId: String) {
        val gameMode = record.gameMode?.name
        val difficulty = record.difficulty?.name

        if (userId.isEmpty() || gameMode == null || difficulty == null) {
            Log.w(TAG, "addRecord: missing userId, gameMode, or difficulty — skipping")
            return
        }

        val documentId = "${userId}_${gameMode}_${difficulty}"
        val recordMap = hashMapOf(
            LEADERBOARD_DOCUMENT_USER_ID_KEY to userId,
            LEADERBOARD_DOCUMENT_SCORE_KEY to record.score,
            LEADERBOARD_DOCUMENT_MODE_KEY to gameMode,
            LEADERBOARD_DOCUMENT_DIFFICULTY_KEY to difficulty,
        )

        leaderboardCollection.document(documentId)
            .set(recordMap)
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    companion object {
        const val COLLECTION_NAME_LEADERBOARD = "leaderboard"

        const val LEADERBOARD_DOCUMENT_USER_ID_KEY = "userId"
        const val LEADERBOARD_DOCUMENT_SCORE_KEY = "score"
        const val LEADERBOARD_DOCUMENT_MODE_KEY = "gameMode"
        const val LEADERBOARD_DOCUMENT_DIFFICULTY_KEY = "difficulty"
    }
}
