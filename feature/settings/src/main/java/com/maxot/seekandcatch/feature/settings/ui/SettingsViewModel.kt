package com.maxot.seekandcatch.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.settings.SCLocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val localeManager: SCLocaleManager
) : ViewModel() {

    val soundState = settingsRepository.observeSoundState()
    val musicState = settingsRepository.observeMusicState()
    val vibrationState = settingsRepository.observeVibrationState()

    var allSupportedLocales = localeManager.getLocales()
    var selectedLocale = localeManager.getSelectedLocale()?.toLanguageTag() ?: "en-US"

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

    fun updateSelectedLocale(locale: String) {
        localeManager.setLocale(locale)
        selectedLocale = localeManager.getSelectedLocale()?.toLanguageTag() ?: "en-US"
        allSupportedLocales = localeManager.getLocales()
    }
}
