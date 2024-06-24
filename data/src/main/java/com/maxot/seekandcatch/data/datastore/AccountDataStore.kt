package com.maxot.seekandcatch.data.datastore

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.accountDataStore: DataStore<Preferences> by preferencesDataStore(name = AccountDataStore.ACCOUNT_DATA_STORE_NAME)

class AccountDataStore
@Inject constructor(
    @ApplicationContext val context: Context
) {
    private val dataStore = context.accountDataStore

    private val userNameKey = stringPreferencesKey(USER_NAME_KEY)
    private val userIdKey = stringPreferencesKey(USER_ID_KEY)
    private val selectedColorsIdKey = stringSetPreferencesKey(SELECTED_COLORS_ID_KEY)

    val userNameFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[userNameKey] ?: ""
    }
    val userIdFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[userIdKey] ?: ""
    }
    val selectedColors: Flow<Set<Color>> = dataStore.data.map { preferences ->
        preferences[selectedColorsIdKey]?.map { argbString ->
            Color(argbString.toInt())
        }?.toSet() ?: setOf(Color.Red, Color.Blue, Color.Yellow, Color.Green)
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { settings ->
            settings[userNameKey] = name
        }
    }

    suspend fun setUserId(name: String) {
        dataStore.edit { settings ->
            settings[userIdKey] = name
        }
    }

    suspend fun setSelectedColors(colors: Set<Color>) {
        dataStore.edit { settings ->
            val colorsSet = mutableSetOf<String>()
            colors.forEach { color ->
                colorsSet.add(color.toArgb().toString())
            }
            settings[selectedColorsIdKey] = colorsSet
        }
    }

    companion object {
        const val ACCOUNT_DATA_STORE_NAME = "Account"
        const val USER_NAME_KEY = "User_name_key"
        const val USER_ID_KEY = "User_id_key"
        const val SELECTED_COLORS_ID_KEY = "Selected_colors_key"
    }
}