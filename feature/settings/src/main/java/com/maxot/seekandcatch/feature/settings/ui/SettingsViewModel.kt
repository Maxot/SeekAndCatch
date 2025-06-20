package com.maxot.seekandcatch.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.core.media.SoundManager
import com.maxot.seekandcatch.core.media.SoundType
import com.maxot.seekandcatch.core.model.DarkThemeConfig
import com.maxot.seekandcatch.data.repository.SettingsRepository
import com.maxot.seekandcatch.feature.settings.SCLocaleManager
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
    private val soundManager: SoundManager
) : ViewModel() {

    val soundState = settingsRepository.observeSoundState()
    val musicState = settingsRepository.observeMusicState()
    val vibrationState = settingsRepository.observeVibrationState()

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
            soundManager.playSound(SoundType.BUTTON_CLICK)
        }
    }

    fun setMusicState(newState: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMusicState(newState)
            soundManager.playSound(SoundType.BUTTON_CLICK)
        }
    }

    fun setVibrationState(newState: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVibrationState(newState)
            soundManager.playSound(SoundType.BUTTON_CLICK)
        }
    }

    fun updateSelectedLocale(locale: String) {
        localeManager.setLocale(locale)
        selectedLocale = localeManager.getSelectedLocale()?.toLanguageTag() ?: "en-US"
        allSupportedLocales = localeManager.getLocales()
        soundManager.playSound(SoundType.BUTTON_CLICK)
    }

    fun setDarkTheme(isDarkTheme: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkTheme(isDarkTheme)
            soundManager.playSound(SoundType.BUTTON_CLICK)
        }
    }
}
