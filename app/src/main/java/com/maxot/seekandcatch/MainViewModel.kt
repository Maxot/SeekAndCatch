package com.maxot.seekandcatch

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val selectedGameDifficulty: StateFlow<GameDifficulty> =
        settingsRepository.observeDifficulty().stateIn(
            viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = GameDifficulty.NORMAL
        )

    fun setSelectedDifficulty(gameDifficulty: GameDifficulty) {
        viewModelScope.launch {
            settingsRepository.setDifficulty(gameDifficulty)
        }
    }
}