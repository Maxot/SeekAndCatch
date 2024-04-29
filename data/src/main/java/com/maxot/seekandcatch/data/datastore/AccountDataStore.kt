package com.maxot.seekandcatch.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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

    val userNameFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[userNameKey] ?: "Unknown"
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { settings ->
            settings[userNameKey] = name
        }
    }

    companion object {
        const val ACCOUNT_DATA_STORE_NAME = "Account"
        const val USER_NAME_KEY = "User_name_key"
    }
}