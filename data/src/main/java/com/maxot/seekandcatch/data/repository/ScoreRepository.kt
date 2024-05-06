package com.maxot.seekandcatch.data.repository

interface ScoreRepository {

    fun setLastScore(score: Int)

    fun setBestScore(score: Int)

    fun getBestScore(): Int

    fun getLastScore(): Int
}
