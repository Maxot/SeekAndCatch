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
    val userName = settingsRepository.observeUserName()

    fun setSoundState(newState: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundState(newState)
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            settingsRepository.setUserName(name)
        }
    }


}
