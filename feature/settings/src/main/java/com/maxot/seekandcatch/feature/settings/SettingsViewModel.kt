package com.maxot.seekandcatch.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxot.seekandcatch.feature.settings.data.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val soundState = settingsDataStore.soundStateFlow
    fun setSoundState(newState: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setSoundState(newState)
        }
    }


}