package com.maxot.seekandcatch.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.media.AudioManager
import com.maxot.seekandcatch.core.media.SoundType
import com.maxot.seekandcatch.core.model.DarkThemeConfig
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.settings.SCLocaleManager
import com.maxot.seekandcatch.feature.settings.VibrationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val localeManager: SCLocaleManager,
    private val audioManager: AudioManager,
    private val vibrationManager: VibrationManager
) : ViewModel() {

    val soundState = settingsRepository.observeSoundState()
    val musicState = settingsRepository.observeMusicState()
    val vibrationState = settingsRepository.observeVibrationState()
    val colorblindMode = settingsRepository.observeColorblindModeEnabled()

    val darkTheme: StateFlow<Boolean> =
        settingsRepository.userConfig.map { when(it.darkThemeConfig){
            DarkThemeConfig.FOLLOW_SYSTEM -> true
            DarkThemeConfig.LIGHT -> false
            DarkThemeConfig.DARK -> true
        } }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    var allSupportedLocales = localeManager.getLocales()
    var selectedLocale = localeManager.getSelectedLocale()?.toLanguageTag() ?: "en-US"

    fun setSoundState(newState: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundState(newState)
            audioManager.onButtonClick()
        }
    }

    fun setMusicState(newState: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMusicState(newState)
            audioManager.onMusicSettingChanged(newState)
            audioManager.onButtonClick()
        }
    }

    fun setVibrationState(newState: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVibrationState(newState)
            if (newState) {
                vibrationManager.vibrate(50)
            }
            audioManager.onButtonClick()
        }
    }

    fun updateSelectedLocale(locale: String) {
        localeManager.setLocale(locale)
        selectedLocale = localeManager.getSelectedLocale()?.toLanguageTag() ?: "en-US"
        allSupportedLocales = localeManager.getLocales()
        audioManager.onButtonClick()
    }

    fun setDarkTheme(isDarkTheme: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkTheme(isDarkTheme)
            audioManager.onButtonClick()
        }
    }

    fun setColorblindModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setColorblindModeEnabled(enabled)
            audioManager.onButtonClick()
        }
    }
}
