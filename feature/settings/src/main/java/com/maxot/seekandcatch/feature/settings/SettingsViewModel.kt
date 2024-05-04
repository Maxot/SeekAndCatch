package com.maxot.seekandcatch.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val soundState = settingsRepository.observeSoundState()
    val musicState = settingsRepository.observeMusicState()
    val vibrationState = settingsRepository.observeVibrationState()

    fun setSoundState(newState: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundState(newState)
        }
    }

    fun setMusicState(newState: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMusicState(newState)
        }
    }

    fun setVibrationState(newState: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVibrationState(newState)
        }
    }
}
