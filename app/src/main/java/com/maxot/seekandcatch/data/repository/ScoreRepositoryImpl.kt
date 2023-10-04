package com.maxot.seekandcatch.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ScoreRepositoryImpl
@Inject constructor(
    @ApplicationContext context: Context
) : ScoreRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    override fun saveBestScore(score: Int) {
        val lastBestScore = getBestScore()
        if (lastBestScore < score) {
            prefs.edit().putInt(scoreKey, score).apply()
        }
    }

    override fun getBestScore(): Int = prefs.getInt(scoreKey, 0)

    companion object {
        const val prefName = "score_prefs"
        const val scoreKey = "score"
    }
}
