package com.maxot.seekandcatch.feature.gameplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.data.model.LeaderboardRecord
import com.maxot.seekandcatch.data.repository.AccountRepository
import com.maxot.seekandcatch.data.repository.LeaderboardRepository
import com.maxot.seekandcatch.data.repository.ScoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameResultViewModel
@Inject constructor(
    private val scoreRepository: ScoreRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val accountRepository: AccountRepository,
) : ViewModel() {
    fun getLastScore() = scoreRepository.getLastScore()
    fun getBestScore() = scoreRepository.getBestScore()

    fun addResultToLeaderboard(score: Int) {
        scoreRepository.setBestScore(score)

        viewModelScope.launch {
            val userName = accountRepository.observeUserName().first()
            leaderboardRepository.addRecord(
                LeaderboardRecord(
                    userName = userName,
                    score = score
                )
            )
        }
    }
}
