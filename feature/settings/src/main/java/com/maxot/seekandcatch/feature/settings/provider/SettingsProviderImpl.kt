package com.maxot.seekandcatch.feature.settings.provider

import com.maxot.seekandcatch.core.media.provider.SettingsProvider
import com.maxot.seekandcatch.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SettingsProviderImpl @Inject constructor(
    private val settingsRepository: SettingsRepository
) : SettingsProvider {
    override suspend fun isSoundEnabled(): Boolean {
        return settingsRepository.observeSoundState().first()
    }

    override suspend fun isMusicEnabled(): Boolean {
        return settingsRepository.observeMusicState().first()
    }
}
