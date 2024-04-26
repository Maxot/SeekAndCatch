package com.maxot.seekandcatch.feature.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.data.model.LeaderboardRecord
import com.maxot.seekandcatch.data.repository.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

const val TAG = "LeaderBoardViewModel"

@HiltViewModel
class LeaderboardViewModel
@Inject constructor(
    private val repository: LeaderboardRepository
) : ViewModel() {

    val leaderboardUiState: StateFlow<LeaderboardUiState> =
        repository.observeRecords()
            .map {
                val sortedList = it.sortedByDescending { leaderboardRecord ->
                    leaderboardRecord.score
                }
                LeaderboardUiState.Successful(sortedList)
            }
            .stateIn(
                viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = LeaderboardUiState.Loading
            )
}

sealed interface LeaderboardUiState {
    data class Successful(val data: List<LeaderboardRecord>) : LeaderboardUiState
    data object Loading : LeaderboardUiState
    data object Failed : LeaderboardUiState
}