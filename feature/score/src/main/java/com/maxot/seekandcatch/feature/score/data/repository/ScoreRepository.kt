package com.maxot.seekandcatch.feature.score.data.repository

interface ScoreRepository {
    fun setScore(score: Int)
    fun getBestScore(): Int
    fun getLastScore(): Int
}