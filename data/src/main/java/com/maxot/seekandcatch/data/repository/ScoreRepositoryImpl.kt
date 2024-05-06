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

    override fun setLastScore(score: Int) {
        prefs.edit().putInt(lastScoreKey, score).apply()
    }

    override fun setBestScore(score: Int) {
        prefs.edit().putInt(bestScoreKey, score).apply()
    }


    override fun getBestScore(): Int = prefs.getInt(bestScoreKey, 0)
    override fun getLastScore(): Int = prefs.getInt(lastScoreKey, 0)

    companion object {
        const val prefName = "score_prefs"
        const val bestScoreKey = "best_score"
        const val lastScoreKey = "last_score"
    }
}
