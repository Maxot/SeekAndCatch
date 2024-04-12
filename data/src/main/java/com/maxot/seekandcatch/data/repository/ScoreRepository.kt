package com.maxot.seekandcatch.data.repository

interface ScoreRepository {
    fun setScore(score: Int)
    fun getBestScore(): Int
    fun getLastScore(): Int
}