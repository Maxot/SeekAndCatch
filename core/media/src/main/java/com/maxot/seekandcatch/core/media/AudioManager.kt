package com.maxot.seekandcatch.core.media

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioManager @Inject constructor(
    private val soundManager: SoundManager,
    private val musicManager: MusicManager
) {
    fun playSound(soundType: SoundType) {
        soundManager.playSound(soundType)
    }

    fun playMusic(musicType: MusicType) {
        musicManager.play(musicType)
    }

    fun pauseMusic() {
        musicManager.pauseMusic()
    }

    fun resumeMusic() {
        musicManager.resumeMusic()
    }

    fun stopMusic() {
        musicManager.stopMusic()
    }

    fun release() {
        // Only stop music and release sound manager, do not cancel MusicManager's scope
        // since AudioManager is a Singleton and should persist across screens.
        stopMusic()
    }

    fun setMusicSpeed(speed: Float) {
        musicManager.setMusicSpeed(speed)
    }

    /**
     * Call this when game starts (countdown begins)
     */
    fun onGameStart() {
        stopMusic()
        playSound(SoundType.COUNTDOWN)
    }

    /**
     * Call this when gameplay actually begins (after countdown)
     */
    fun onGameplayStarted() {
        playMusic(MusicType.GAME)
    }

    /**
     * Call this when game is paused
     */
    fun onGamePaused() {
        pauseMusic()
    }

    /**
     * Call this when game is resumed
     */
    fun onGameResumed() {
        resumeMusic()
    }

    /**
     * Call this when game is over
     */
    fun onGameOver() {
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
