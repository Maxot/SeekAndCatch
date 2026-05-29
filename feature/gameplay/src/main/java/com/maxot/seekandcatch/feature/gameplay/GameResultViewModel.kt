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
import com.maxot.seekandcatch.core.media.AudioManager
import com.maxot.seekandcatch.core.media.SoundType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val audioManager: AudioManager,
) : ViewModel() {

    private val score: Int = checkNotNull(savedStateHandle[SCORE_ARG])

    private val _uiState = MutableStateFlow(
        GameResultUiState(
            userName = "",
            lastScore = score
        )
    )
    val uiState: StateFlow<GameResultUiState> = _uiState

    private var scoreSubmitted = false

    init {
        audioManager.stopMusic()

        viewModelScope.launch {
            // Capture game context once — settings changes after this point cannot affect submission
            val mode = settingsRepository.observeGameMode().first()
            val difficulty = settingsRepository.observeDifficulty().first()
            val userId = authRepository.getUserId()

            _uiState.update { it.copy(gameMode = mode, gameDifficulty = difficulty) }

            // Only observeRecords() drives reactivity from here
            leaderboardRepository.observeRecords().collectLatest { records ->
                val inContext = records.asSequence()
                    .filter { it.gameMode == mode && it.difficulty == difficulty }

                val remoteBest = inContext
                    .filter { it.userId == userId }
                    .mapNotNull { it.score }
                    .maxOrNull() ?: 0

                val rank = inContext
                    .sortedByDescending { it.score }
                    .toList()
                    .indexOfFirst { it.userId == userId }
                    .let { if (it >= 0) it + 1 else null }

                _uiState.update {
                    it.copy(
                        remoteBestForContext = remoteBest,
                        isNewBest = it.lastScore > remoteBest || it.isNewBest,
                        rank = rank
                    )
                }

                autoSubmitScore(mode, difficulty, userId)
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
            audioManager.playSound(SoundType.NEW_BEST_SCORE)
            _uiState.update { it.copy(hasPlayedNewBestSound = true) }
        }
    }

    private fun handleRestart() {
        // Placeholder for any state updates before navigation if needed
    }

    private fun handleContinue() {
        // Score submission is already handled by autoSubmitScore
    }

    private suspend fun autoSubmitScore(mode: GameMode, difficulty: GameDifficulty, userId: String) {
        val current = _uiState.value
        if (!scoreSubmitted && current.lastScore > current.remoteBestForContext) {
            _uiState.update { it.copy(isProcessing = true) }
            if (!scoreSubmitted && current.lastScore > _uiState.value.remoteBestForContext) {
                scoreSubmitted = true
                leaderboardRepository.addRecord(
                    LeaderboardRecord(
                        userId = userId,
                        userName = current.userName,
                        score = current.lastScore,
                        gameMode = mode,
                        difficulty = difficulty
                    )
                )
            }
            _uiState.update { it.copy(isProcessing = false) }
        }
    }
}
