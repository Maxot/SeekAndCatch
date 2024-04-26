package com.maxot.seekandcatch.data.repository

import com.maxot.seekandcatch.data.model.GameDifficulty
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    suspend fun setSoundState(newState: Boolean)

    fun observeSoundState(): Flow<Boolean>

    suspend fun setDifficulty(newDifficulty: GameDifficulty)

    fun observeDifficulty(): Flow<GameDifficulty>

    suspend fun setUserName(name: String)

    fun observeUserName(): Flow<String>

}