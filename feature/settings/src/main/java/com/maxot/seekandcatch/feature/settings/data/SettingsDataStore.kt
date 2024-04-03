package com.maxot.seekandcatch.feature.settings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = SettingsDataStore.SETTINGS_DATA_STORE_NAME)

class SettingsDataStore
@Inject constructor(
    @ApplicationContext val context: Context
) {

    private val soundStateKey = booleanPreferencesKey(SETTINGS_SOUND_KEY)
    val soundStateFlow: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[soundStateKey] ?: true
    }

    suspend fun setSoundState(newState: Boolean) {
        context.settingsDataStore.edit { settings ->
            settings[soundStateKey] = newState
        }
    }

    companion object {
        const val SETTINGS_DATA_STORE_NAME = "Settings"
        const val SETTINGS_SOUND_KEY = "Sound_key"
    }
}
