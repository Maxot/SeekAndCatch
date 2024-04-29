package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.data.datastore.SettingsDataStore
import com.maxot.seekandcatch.data.model.GameDifficulty
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl
@Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {
    override suspend fun setSoundState(newState: Boolean) {
        settingsDataStore.setSoundState(newState = newState)
    }

    override fun observeSoundState(): Flow<Boolean> = settingsDataStore.soundStateFlow

    override suspend fun setDifficulty(newDifficulty: GameDifficulty) {
        settingsDataStore.setDifficulty(newDifficulty)
    }

    override fun observeDifficulty(): Flow<GameDifficulty> = settingsDataStore.difficultyFlow

}
