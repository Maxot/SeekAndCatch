package com.maxot.seekandcatch.feature.leaderboard

import androidx.lifecycle.ViewModel
import com.maxot.seekandcatch.data.model.Leader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LeaderBoardViewModel
@Inject constructor() : ViewModel() {

    private val _leaders = MutableStateFlow(
        listOf(
            Leader(userId = 1, name = "Max", score = 555),
            Leader(userId = 2, name = "John", score = 435),
            Leader(userId = 3, name = "Piter", score = 235),
            Leader(userId = 4, name = "Stas", score = 125),
        )
    )
    val leaders: StateFlow<List<Leader>>
        get() = _leaders

}