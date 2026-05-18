package com.maxot.seekandcatch.feature.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode
import com.maxot.seekandcatch.core.domain.user.UserUseCase
import com.maxot.seekandcatch.data.repository.LeaderboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class LeaderboardViewModel(
    private val userUseCase: UserUseCase,
    private val repository: LeaderboardRepository
) : ViewModel() {

    private val selectedMode = MutableStateFlow<GameMode?>(null)
    private val selectedDifficulty = MutableStateFlow<GameDifficulty?>(null)

    private val allRecords = repository.observeRecords()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val leaderboardUiState: StateFlow<LeaderboardUiState> =
        combine(allRecords, selectedMode, selectedDifficulty) { list, mode, difficulty ->
            val filtered = list.asSequence()
                .filter { record -> mode == null || record.gameMode == mode }
                .filter { record -> difficulty == null || record.difficulty == difficulty }
                .sortedByDescending { it.score ?: 0 }
                .toList()
            LeaderboardUiState.Successful(
                data = filtered,
                selectedMode = mode,
                userData = userUseCase.getUser(),
                selectedDifficulty = difficulty
            ) as LeaderboardUiState
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            LeaderboardUiState.Loading
        )

    fun onEvent(event: LeaderboardEvent) {
        when (event) {
            is LeaderboardEvent.SelectMode -> selectedMode.update { event.mode }
            is LeaderboardEvent.SelectDifficulty -> selectedDifficulty.update { event.difficulty }
        }
    }
}
