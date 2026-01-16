package com.maxot.seekandcatch.feature.gameplay

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.maxot.seekandcatch.feature.gameplay.navigation.SCORE_ARG
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.data.model.GameMode
import com.maxot.seekandcatch.data.model.LeaderboardRecord
import com.maxot.seekandcatch.data.repository.AccountRepository
import com.maxot.seekandcatch.data.repository.LeaderboardRepository
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
    savedStateHandle: SavedStateHandle,
    private val leaderboardRepository: LeaderboardRepository,
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val score: Int = checkNotNull(savedStateHandle[SCORE_ARG])

    private val _uiState = MutableStateFlow(
        GameResultUiState(
            userName = "",
            lastScore = score
        )
    )
    val uiState: StateFlow<GameResultUiState> = _uiState

    init {
        // Keep username in sync and auto-submit when it changes
        viewModelScope.launch {
            accountRepository.observeUserName().collectLatest { name ->
                _uiState.update { it.copy(userName = name) }
                if (name.isNotBlank()) {
                    autoSubmitScore()
                }
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
                autoSubmitScore()
            }
        }
    }

    fun onEvent(event: GameResultEvent) {
        when (event) {
            is GameResultEvent.ContinueClicked -> handleContinue()
            is GameResultEvent.DismissUserNameDialog -> _uiState.update { it.copy(showUserNameDialog = false) }
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            accountRepository.setUserName(name)
        }
    }

    private fun handleContinue() {
        // Score submission is already handled by autoSubmitScore
    }

    private fun autoSubmitScore() {
        val current = _uiState.value
        val score = current.lastScore

        // Submit to remote leaderboard if it's a new best
        if (score > current.remoteBestForContext) {
            // Ensure user has a name
            if (current.userName.isBlank()) {
                _uiState.update { it.copy(showUserNameDialog = true) }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isProcessing = true) }
                val mode: GameMode = settingsRepository.observeGameMode().first()
                val difficulty: GameDifficulty = settingsRepository.observeDifficulty().first()

                // Re-verify it's still a new best before adding
                if (score > _uiState.value.remoteBestForContext) {
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
}
