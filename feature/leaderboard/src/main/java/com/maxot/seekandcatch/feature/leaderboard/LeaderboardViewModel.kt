package com.maxot.seekandcatch.feature.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode
import com.maxot.seekandcatch.core.domain.user.UserUseCase
import com.maxot.seekandcatch.data.repository.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

const val TAG = "LeaderBoardViewModel"

@HiltViewModel
class LeaderboardViewModel
@Inject constructor(
    private val userUseCase: UserUseCase,
    private val repository: LeaderboardRepository
) : ViewModel() {

    // Internal selection state (MVI inputs)
    private val selectedMode = MutableStateFlow<GameMode?>(null) // null = All modes
    private val selectedDifficulty = MutableStateFlow<GameDifficulty?>(null) // null = All difficulty

    // Single UI state flow (MVI output)
    val leaderboardUiState: StateFlow<LeaderboardUiState> =
        combine(
            repository.observeRecords(),
            selectedMode,
            selectedDifficulty
        ) { list, mode, difficulty ->
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
        }
        .catch { emit(LeaderboardUiState.Failed) }
        .stateIn(
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
