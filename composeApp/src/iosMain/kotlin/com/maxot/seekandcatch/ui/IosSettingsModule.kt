package com.maxot.seekandcatch.ui

import com.maxot.seekandcatch.feature.settings.AudioController
import com.maxot.seekandcatch.feature.settings.HapticsController
import com.maxot.seekandcatch.feature.settings.LocaleController
import org.koin.dsl.module

// ---------------------------------------------------------------------------
// Stub: AudioController — no-op on iOS (media layer not implemented yet)
// ---------------------------------------------------------------------------
private class IosAudioController : AudioController {
    override fun onButtonClick() = Unit
    override fun onMusicSettingChanged(enabled: Boolean) = Unit
    override fun onGameStart() = Unit
    override fun onGameplayStarted() = Unit
    override fun onGamePaused() = Unit
    override fun onGameResumed() = Unit
    override fun onGameOver() = Unit
    override fun onCorrectTap() = Unit
    override fun onMiss() = Unit
    override fun playMenuMusic() = Unit
    override fun pauseMusic() = Unit
    override fun resumeMusic() = Unit
    override fun stopMusic() = Unit
    override fun release() = Unit
    override fun playNewBestScore() = Unit
}

// ---------------------------------------------------------------------------
// Stub: HapticsController — no-op on iOS (haptics not implemented yet)
// ---------------------------------------------------------------------------
private class IosHapticsController : HapticsController {
    override suspend fun vibrate(duration: Long) = Unit
    override suspend fun vibrateCorrect() = Unit
    override suspend fun vibrateError() = Unit
}

// ---------------------------------------------------------------------------
// Stub: LocaleController — single locale on iOS for now
// ---------------------------------------------------------------------------
private class IosLocaleController : LocaleController {
    override fun setLocale(languageTag: String) = Unit
    override fun getLocales(): List<String> = listOf("en")
    override fun getSelectedLocaleTag(): String = "en"
}

// ---------------------------------------------------------------------------
// Koin module
// ---------------------------------------------------------------------------
val iosSettingsModule = module {
    single<AudioController> { IosAudioController() }
    single<HapticsController> { IosHapticsController() }
    single<LocaleController> { IosLocaleController() }
}
