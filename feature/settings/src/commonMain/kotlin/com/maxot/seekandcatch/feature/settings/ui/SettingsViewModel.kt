package com.maxot.seekandcatch.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.model.DarkThemeConfig
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.settings.AudioController
import com.maxot.seekandcatch.feature.settings.HapticsController
import com.maxot.seekandcatch.feature.settings.LocaleController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val localeController: LocaleController,
    private val audioController: AudioController,
    private val hapticsController: HapticsController
) : ViewModel() {

    val soundState = settingsRepository.observeSoundState()
    val musicState = settingsRepository.observeMusicState()
    val vibrationState = settingsRepository.observeVibrationState()
    val colorblindMode = settingsRepository.observeColorblindModeEnabled()

    val darkTheme: StateFlow<Boolean> =
        settingsRepository.userConfig.map {
            when (it.darkThemeConfig) {
                DarkThemeConfig.FOLLOW_SYSTEM -> true
                DarkThemeConfig.LIGHT -> false
                DarkThemeConfig.DARK -> true
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    var allSupportedLocales = localeController.getLocales()
    var selectedLocale = localeController.getSelectedLocaleTag() ?: "en-US"

    fun setSoundState(newState: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundState(newState)
            audioController.onButtonClick()
        }
    }

    fun setMusicState(newState: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMusicState(newState)
            audioController.onMusicSettingChanged(newState)
            audioController.onButtonClick()
        }
    }

    fun setVibrationState(newState: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVibrationState(newState)
            if (newState) {
                hapticsController.vibrate(50)
            }
            audioController.onButtonClick()
        }
    }

    fun updateSelectedLocale(locale: String) {
        localeController.setLocale(locale)
        selectedLocale = localeController.getSelectedLocaleTag() ?: "en-US"
        allSupportedLocales = localeController.getLocales()
        audioController.onButtonClick()
    }

    fun setDarkTheme(isDarkTheme: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkTheme(isDarkTheme)
            audioController.onButtonClick()
        }
    }

    fun setColorblindModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setColorblindModeEnabled(enabled)
            audioController.onButtonClick()
        }
    }
}
