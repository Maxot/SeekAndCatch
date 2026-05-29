package com.maxot.seekandcatch.core.media

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioManager @Inject constructor(
    private val soundManager: SoundManager,
    private val musicManager: MusicManager
) {
    // Prevents MusicController.onResume() from restarting music while a game is paused.
    @Volatile private var isGamePaused = false

    fun playSound(soundType: SoundType) {
        soundManager.playSound(soundType)
    }

    fun playMusic(musicType: MusicType) {
        musicManager.play(musicType)
    }

    fun onMusicSettingChanged(enabled: Boolean) {
        musicManager.onMusicSettingChanged(enabled)
    }

    fun pauseMusic() {
        musicManager.pauseMusic()
    }

    fun resumeMusic() {
        if (isGamePaused) return
        musicManager.resumeMusic()
    }

    fun stopMusic() {
        musicManager.stopMusic()
    }

    fun release() {
        isGamePaused = false
        stopMusic()
    }

    fun setMusicSpeed(speed: Float) {
        musicManager.setMusicSpeed(speed)
    }

    fun onGameStart() {
        isGamePaused = false
        stopMusic()
        playSound(SoundType.COUNTDOWN)
    }

    fun onGameplayStarted() {
        playMusic(MusicType.GAME)
    }

    fun onGamePaused() {
        isGamePaused = true
        pauseMusic()
    }

    fun onGameResumed() {
        isGamePaused = false
        resumeMusic()
    }

    fun onGameOver() {
        isGamePaused = false
        stopMusic()
        playSound(SoundType.GAME_OVER)
    }

    fun onCorrectTap() {
        playSound(SoundType.FIGURE_CLICK)
    }

    fun onMiss() {
        playSound(SoundType.MISS)
    }

    fun onButtonClick() {
        playSound(SoundType.BUTTON_CLICK)
    }
}
