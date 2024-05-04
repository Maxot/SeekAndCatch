package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.data.datastore.SettingsDataStore
import com.maxot.seekandcatch.data.model.GameDifficulty
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl
@Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {
    override suspend fun setSoundState(newState: Boolean) =
        settingsDataStore.setSoundState(newState = newState)


    override fun observeSoundState(): Flow<Boolean> = settingsDataStore.soundStateFlow
    override suspend fun setMusicState(newState: Boolean) =
        settingsDataStore.setMusicState(newState = newState)

    override fun observeMusicState(): Flow<Boolean> = settingsDataStore.musicStateFlow

    override suspend fun setVibrationState(newState: Boolean) =
        settingsDataStore.setVibrationState(newState = newState)

    override fun observeVibrationState(): Flow<Boolean> = settingsDataStore.vibrationStateFlow

    override suspend fun setDifficulty(newDifficulty: GameDifficulty) {
        settingsDataStore.setDifficulty(newDifficulty)
    }

    override fun observeDifficulty(): Flow<GameDifficulty> = settingsDataStore.difficultyFlow

}
