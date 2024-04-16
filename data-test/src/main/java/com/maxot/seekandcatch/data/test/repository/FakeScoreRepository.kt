package com.maxot.seekandcatch.data.test.repository

import com.maxot.seekandcatch.data.repository.ScoreRepository

class FakeScoreRepository : ScoreRepository {

    private var bestScore: Int = 0
    private var lastScore: Int = 0

    override fun setScore(score: Int) {
        lastScore = score
        bestScore = maxOf(bestScore, score)
    }

    override fun getBestScore(): Int = bestScore


    override fun getLastScore(): Int = lastScore

}