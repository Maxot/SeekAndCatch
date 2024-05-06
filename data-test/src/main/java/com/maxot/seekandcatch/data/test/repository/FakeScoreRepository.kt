package com.maxot.seekandcatch.data.test.repository

import com.maxot.seekandcatch.data.repository.ScoreRepository

class FakeScoreRepository : ScoreRepository {

    private var bestScore: Int = 0
    private var lastScore: Int = 0

    override fun setLastScore(score: Int) {
        lastScore = score
    }

    override fun setBestScore(score: Int) {
        bestScore = score
    }

    override fun getBestScore(): Int = bestScore


    override fun getLastScore(): Int = lastScore

}