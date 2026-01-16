package com.maxot.seekandcatch.feature.gameplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.data.model.GameMode
import com.maxot.seekandcatch.data.model.LeaderboardRecord
import com.maxot.seekandcatch.data.repository.AccountRepository
import com.maxot.seekandcatch.data.repository.LeaderboardRepository
import com.maxot.seekandcatch.data.repository.ScoreRepository
import com.maxot.seekandcatch.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameResultViewModel
@Inject constructor(
    private val scoreRepository: ScoreRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GameResultUiState(
            userName = "",
            lastScore = scoreRepository.getLastScore(),
            bestScore = scoreRepository.getBestScore()
        )
    )
    val uiState: StateFlow<GameResultUiState> = _uiState

    init {
        // Keep username in sync
        viewModelScope.launch {
            accountRepository.observeUserName().collectLatest { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }

        // Track remote best for current user, mode and difficulty
        viewModelScope.launch {
            combine(
                accountRepository.observeUserName(),
                settingsRepository.observeGameMode(),
                settingsRepository.observeDifficulty(),
                leaderboardRepository.observeRecords()
            ) { name, mode, difficulty, records ->
                val best = records.asSequence()
                    .filter { it.gameMode == mode && it.difficulty == difficulty }
                    .filter { it.userName == name }
                    .mapNotNull { it.score }
                    .maxOrNull() ?: 0
                best
            }.collectLatest { remoteBest ->
                _uiState.update { it.copy(remoteBestForContext = remoteBest) }
            }
        }
    }

    fun onEvent(event: GameResultEvent) {
        when (event) {
            is GameResultEvent.AddToLeaderboardClicked -> handleAddToLeaderboard()
            is GameResultEvent.ContinueClicked -> handleContinue()
            is GameResultEvent.DismissUserNameDialog -> _uiState.update { it.copy(showUserNameDialog = false) }
        }
    }

    private fun handleContinue() {
        val score = _uiState.value.lastScore
        val best = _uiState.value.bestScore
        if (score > best) {
            scoreRepository.setBestScore(score)
            _uiState.update { it.copy(bestScore = score) }
        }
    }

    private fun handleAddToLeaderboard() {
        val current = _uiState.value
        val score = current.lastScore
        // Update best if needed
        if (score > current.bestScore) {
            scoreRepository.setBestScore(score)
            _uiState.update { it.copy(bestScore = score) }
        }
        // Ensure user has a name
        if (current.userName.isBlank()) {
            _uiState.update { it.copy(showUserNameDialog = true) }
            return
        }
        // Submit to leaderboard
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            val mode: GameMode = settingsRepository.observeGameMode().first()
            val difficulty: GameDifficulty = settingsRepository.observeDifficulty().first()

            val remoteBestForContext = _uiState.value.remoteBestForContext

            if (score >= remoteBestForContext) {
                leaderboardRepository.addRecord(
                    LeaderboardRecord(
                        userName = current.userName,
                        score = score,
                        gameMode = mode,
                        difficulty = difficulty
                    )
                )
            }
            _uiState.update { it.copy(isProcessing = false) }
        }
    }
}
