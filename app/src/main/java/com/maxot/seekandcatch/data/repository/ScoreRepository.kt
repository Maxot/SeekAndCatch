package com.maxot.seekandcatch.data.repository

interface ScoreRepository {

    fun saveBestScore(score: Int)
    fun getBestScore(): Int
}