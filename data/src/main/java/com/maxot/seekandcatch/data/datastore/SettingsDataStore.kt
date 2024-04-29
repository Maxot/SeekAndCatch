package com.maxot.seekandcatch.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.maxot.seekandcatch.data.model.GameDifficulty
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = SettingsDataStore.SETTINGS_DATA_STORE_NAME)

class SettingsDataStore
@Inject constructor(
    @ApplicationContext val context: Context
) {
    private val dataStore = context.settingsDataStore

    private val soundStateKey = booleanPreferencesKey(SETTINGS_SOUND_KEY)
    private val difficultyKey = stringPreferencesKey(SETTINGS_DIFFICULTY_KEY)

    val soundStateFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[soundStateKey] ?: true
    }

    suspend fun setSoundState(newState: Boolean) {
        dataStore.edit { settings ->
            settings[soundStateKey] = newState
        }
    }

    val difficultyFlow: Flow<GameDifficulty> = dataStore.data.map { preferences ->
        val enumName = preferences[difficultyKey]
        enumName?.let {
            GameDifficulty.valueOf(it)
        } ?: GameDifficulty.NORMAL
    }

    suspend fun setDifficulty(newDifficulty: GameDifficulty) {
        dataStore.edit { settings ->
            settings[difficultyKey] = newDifficulty.name
        }
    }


    companion object {
        const val SETTINGS_DATA_STORE_NAME = "Settings"
        const val SETTINGS_SOUND_KEY = "Sound_key"
        const val SETTINGS_DIFFICULTY_KEY = "Difficulty_key"
    }
}
