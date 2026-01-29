package com.maxot.seekandcatch.feature.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode
import com.maxot.seekandcatch.data.repository.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

const val TAG = "LeaderBoardViewModel"

@HiltViewModel
class LeaderboardViewModel
@Inject constructor(
    private val repository: LeaderboardRepository
) : ViewModel() {

    // Internal selection state (MVI inputs)
    private val selectedMode = MutableStateFlow<GameMode?>(null) // null = All modes
    private val selectedDifficulty = MutableStateFlow<GameDifficulty?>(null) // null = All difficulty

    // Source of truth: all records
    private val allRecords = repository.observeRecords()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Single UI state flow (MVI output)
    val leaderboardUiState: StateFlow<LeaderboardUiState> =
        combine(allRecords, selectedMode, selectedDifficulty) { list, mode, difficulty ->
            val filtered = list.asSequence()
                .filter { record ->
                    // Mode filter: if a specific mode is selected, include only matching; if All (null), include all (even nulls)
                    mode == null || record.gameMode == mode
                }
                .filter { record ->
                    // Difficulty filter: if a specific difficulty is selected, include only matching; if All (null), include all (even nulls)
                    difficulty == null || record.difficulty == difficulty
                }
                .sortedByDescending { it.score ?: 0 }
                .toList()
            LeaderboardUiState.Successful(
                data = filtered,
                selectedMode = mode,
                selectedDifficulty = difficulty
            ) as LeaderboardUiState
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            LeaderboardUiState.Loading
        )

    // Single public entrypoint to mutate state
    fun onEvent(event: LeaderboardEvent) {
        when (event) {
            is LeaderboardEvent.SelectMode -> selectedMode.update { event.mode }
            is LeaderboardEvent.SelectDifficulty -> selectedDifficulty.update { event.difficulty }
        }
    }

}
