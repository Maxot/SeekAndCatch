package com.maxot.seekandcatch.feature.score

import androidx.lifecycle.ViewModel
import com.maxot.seekandcatch.feature.score.data.repository.ScoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScoreViewModel
@Inject constructor(
    private val scoreRepository: ScoreRepository
) : ViewModel() {
    fun getLastScore() = scoreRepository.getLastScore()
    fun getBestScore() = scoreRepository.getBestScore()
}