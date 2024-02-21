package com.maxot.seekandcatch.feature.score.data.repository

interface ScoreRepository {

    fun saveBestScore(score: Int)
    fun getBestScore(): Int
}