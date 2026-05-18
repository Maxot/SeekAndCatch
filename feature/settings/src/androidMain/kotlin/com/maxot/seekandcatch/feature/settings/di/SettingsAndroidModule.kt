package com.maxot.seekandcatch.feature.settings.di

import com.maxot.seekandcatch.core.media.AudioManager
import com.maxot.seekandcatch.core.media.MusicManager
import com.maxot.seekandcatch.core.media.SoundManager
import com.maxot.seekandcatch.core.media.provider.SettingsProvider
import com.maxot.seekandcatch.feature.settings.AudioController
import com.maxot.seekandcatch.feature.settings.AudioControllerImpl
import com.maxot.seekandcatch.feature.settings.HapticsController
import com.maxot.seekandcatch.feature.settings.LocaleController
import com.maxot.seekandcatch.feature.settings.SCLocaleManager
import com.maxot.seekandcatch.feature.settings.VibrationManager
import com.maxot.seekandcatch.feature.settings.provider.SettingsProviderImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val settingsAndroidModule = module {
    single<SettingsProvider> { SettingsProviderImpl(get()) }
    single { SoundManager(androidContext(), get()) }
    single { MusicManager(androidContext(), get()) }
    single { AudioManager(get(), get()) }
    single<AudioController> { AudioControllerImpl(get()) }
    single<HapticsController> { VibrationManager(androidContext(), get()) }
    single<LocaleController> { SCLocaleManager() }
}
