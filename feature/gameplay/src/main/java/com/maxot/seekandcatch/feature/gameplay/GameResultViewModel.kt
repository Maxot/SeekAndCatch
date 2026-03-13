package com.maxot.seekandcatch.feature.gameplay

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode
import com.maxot.seekandcatch.core.common.model.LeaderboardRecord
import com.maxot.seekandcatch.data.repository.AuthRepository
import com.maxot.seekandcatch.data.repository.LeaderboardRepository
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.gameplay.navigation.SCORE_ARG
import com.maxot.seekandcatch.core.media.SoundManager
import com.maxot.seekandcatch.core.media.SoundType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameResultViewModel
@Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val leaderboardRepository: LeaderboardRepository,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val soundManager: SoundManager,
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
        // Observe game mode and difficulty for restarting with same settings
        viewModelScope.launch {
            combine(
                settingsRepository.observeGameMode(),
                settingsRepository.observeDifficulty()
            ) { mode, difficulty ->
                mode to difficulty
            }.collectLatest { (mode, difficulty) ->
                _uiState.update {
                    it.copy(
                        gameMode = mode,
                        gameDifficulty = difficulty
                    )
                }
            }
        }

        // Track remote best for current user, mode and difficulty
        viewModelScope.launch {
            val userId = authRepository.getUserId()
            combine(
                settingsRepository.observeGameMode(),
                settingsRepository.observeDifficulty(),
                leaderboardRepository.observeRecords()
            ) { mode, difficulty, records ->
                val best = records.asSequence()
                    .filter { it.gameMode == mode && it.difficulty == difficulty }
                    .filter { it.userId == userId }
                    .mapNotNull { it.score }
                    .maxOrNull() ?: 0
                best
            }.collectLatest { remoteBest ->
                _uiState.update {
                    it.copy(
                        remoteBestForContext = remoteBest,
                        isNewBest = it.lastScore > remoteBest || it.isNewBest
                    )
                }
                autoSubmitScore()
            }
        }
    }

    fun onEvent(event: GameResultEvent) {
        when (event) {
            is GameResultEvent.ContinueClicked -> handleContinue()
            is GameResultEvent.RestartClicked -> handleRestart()
            is GameResultEvent.NewBestSoundPlayed -> handleNewBestSoundPlayed()
        }
    }

    private fun handleNewBestSoundPlayed() {
        if (!_uiState.value.hasPlayedNewBestSound && _uiState.value.isNewBest) {
            soundManager.playSound(SoundType.NEW_BEST_SCORE)
            _uiState.update { it.copy(hasPlayedNewBestSound = true) }
        }
    }

    private fun handleRestart() {
        // Placeholder for any state updates before navigation if needed
    }

    private fun handleContinue() {
        // Score submission is already handled by autoSubmitScore
    }

    private fun autoSubmitScore() {
        val current = _uiState.value
        val score = current.lastScore

        // Submit to remote leaderboard if it's a new best
        if (score > current.remoteBestForContext) {
            viewModelScope.launch {
                val userId = authRepository.getUserId()

                _uiState.update { it.copy(isProcessing = true) }
                val mode: GameMode = settingsRepository.observeGameMode().first()
                val difficulty: GameDifficulty = settingsRepository.observeDifficulty().first()

                // Re-verify it's still a new best before adding
                if (score > _uiState.value.remoteBestForContext) {
                    leaderboardRepository.addRecord(
                        LeaderboardRecord(
                            userId = userId,
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
