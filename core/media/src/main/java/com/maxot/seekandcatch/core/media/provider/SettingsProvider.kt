package com.maxot.seekandcatch.core.media.provider

interface SettingsProvider {
    suspend fun isSoundEnabled(): Boolean
    suspend fun isMusicEnabled(): Boolean
}
