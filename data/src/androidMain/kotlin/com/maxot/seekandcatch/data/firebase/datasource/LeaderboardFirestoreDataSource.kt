package com.maxot.seekandcatch.data.firebase.datasource

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import com.maxot.seekandcatch.core.common.model.LeaderboardRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val TAG = "LeaderboardFirestoreDataSource"

class LeaderboardFirestoreDataSource() : LeaderboardDataSource {

    private val db = Firebase.firestore

    private val leaderboardCollection = db.collection(COLLECTION_NAME_LEADERBOARD)

    override fun observeRecords(): Flow<List<LeaderboardRecord>> =
        leaderboardCollection.snapshots().map { querySnapshot ->
            querySnapshot.toObjects<LeaderboardRecord>()
        }

    override fun addRecord(
        record: LeaderboardRecord,
        userId: String,
        onSuccessful: (String) -> Unit
    ) {
        val recordMap = hashMapOf<String, Any?>(
            LEADERBOARD_DOCUMENT_USER_ID_KEY to userId,
            LEADERBOARD_DOCUMENT_SCORE_KEY to record.score,
        )
        record.gameMode?.name?.let { recordMap[LEADERBOARD_DOCUMENT_MODE_KEY] = it }
        record.difficulty?.name?.let { recordMap[LEADERBOARD_DOCUMENT_DIFFICULTY_KEY] = it }

        if (userId.isNotEmpty()) {
            val userDocument = leaderboardCollection.document(userId)
            userDocument.set(recordMap)
                .addOnSuccessListener {
                    println("D/$TAG: DocumentSnapshot successfully written!")
                }
                .addOnFailureListener { e -> println("W/$TAG: Error writing document $e") }
        } else {
            leaderboardCollection
                .add(recordMap)
                .addOnSuccessListener { documentReference ->
                    onSuccessful(documentReference.id)
                    println("D/$TAG: DocumentSnapshot successfully written!")
                }
                .addOnFailureListener { e -> println("W/$TAG: Error writing document $e") }
        }
    }

    companion object {
        const val COLLECTION_NAME_LEADERBOARD = "leaderboard"

        const val LEADERBOARD_DOCUMENT_USER_ID_KEY = "userId"
        const val LEADERBOARD_DOCUMENT_USER_NAME_KEY = "userName"
        const val LEADERBOARD_DOCUMENT_SCORE_KEY = "score"
        const val LEADERBOARD_DOCUMENT_MODE_KEY = "gameMode"
        const val LEADERBOARD_DOCUMENT_DIFFICULTY_KEY = "difficulty"
    }
}
