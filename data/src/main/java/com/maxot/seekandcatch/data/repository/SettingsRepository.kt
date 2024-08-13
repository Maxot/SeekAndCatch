package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.data.model.GameMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    suspend fun setSoundState(newState: Boolean)

    fun observeSoundState(): Flow<Boolean>

    suspend fun setMusicState(newState: Boolean)

    fun observeMusicState(): Flow<Boolean>

    suspend fun setVibrationState(newState: Boolean)

    fun observeVibrationState(): Flow<Boolean>

    suspend fun setDifficulty(newDifficulty: GameDifficulty)

    fun observeDifficulty(): Flow<GameDifficulty>

    suspend fun setGameMode(gameMode: GameMode)

    fun observeGameMode(): Flow<GameMode>

}
