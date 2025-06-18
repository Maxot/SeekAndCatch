package com.maxot.seekandcatch.feature.settings.di

import com.maxot.seekandcatch.core.media.provider.SettingsProvider
import com.maxot.seekandcatch.feature.settings.provider.SettingsProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {
    @Binds
    abstract fun bindSoundSettingsProvider(
        impl: SettingsProviderImpl
    ): SettingsProvider
}
