package com.maxot.seekandcatch.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.data.model.GameMode
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
    private val musicStateKey = booleanPreferencesKey(SETTINGS_MUSIC_KEY)
    private val vibrationStateKey = booleanPreferencesKey(SETTINGS_VIBRATION_KEY)
    private val difficultyKey = stringPreferencesKey(SETTINGS_DIFFICULTY_KEY)
    private val gameModeKey = stringPreferencesKey(SETTINGS_GAME_MODE_KEY)

    val soundStateFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[soundStateKey] ?: true
    }
    val musicStateFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[musicStateKey] ?: true
    }
    val vibrationStateFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[vibrationStateKey] ?: true
    }
    val difficultyFlow: Flow<GameDifficulty> = dataStore.data.map { preferences ->
        val enumName = preferences[difficultyKey]
        enumName?.let {
            GameDifficulty.valueOf(it)
        } ?: GameDifficulty.NORMAL
    }
    val gameModeFlow: Flow<GameMode> = dataStore.data.map { preferences ->
        val enumName = preferences[gameModeKey]
        enumName?.let {
            GameMode.valueOf(it)
        } ?: GameMode.FLOW
    }

    suspend fun setSoundState(newState: Boolean) {
        dataStore.edit { settings ->
            settings[soundStateKey] = newState
        }
    }

    suspend fun setMusicState(newState: Boolean) {
        dataStore.edit { settings ->
            settings[musicStateKey] = newState
        }
    }

    suspend fun setVibrationState(newState: Boolean) {
        dataStore.edit { settings ->
            settings[vibrationStateKey] = newState
        }
    }

    suspend fun setDifficulty(newDifficulty: GameDifficulty) {
        dataStore.edit { settings ->
            settings[difficultyKey] = newDifficulty.name
        }
    }

    suspend fun setGameMode(gameMode: GameMode) {
        dataStore.edit { settings ->
            settings[gameModeKey] = gameMode.name
        }
    }


    companion object {
        const val SETTINGS_DATA_STORE_NAME = "Settings"
        const val SETTINGS_SOUND_KEY = "Sound_key"
        const val SETTINGS_MUSIC_KEY = "Sound_music"
        const val SETTINGS_VIBRATION_KEY = "Sound_vibration"
        const val SETTINGS_DIFFICULTY_KEY = "Difficulty_key"
        const val SETTINGS_GAME_MODE_KEY = "Game_mode_key"
    }
}
