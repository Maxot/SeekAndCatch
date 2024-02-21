package com.maxot.seekandcatch.feature.score

import androidx.lifecycle.ViewModel
import com.maxot.seekandcatch.feature.score.data.repository.ScoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ScoreViewModel
@Inject constructor(
    private val scoreRepository: ScoreRepository
) : ViewModel() {

    private var _lastScore = MutableStateFlow(0)
    val lastScore: StateFlow<Int> = _lastScore

    fun getBestScore() = scoreRepository.getBestScore()
}