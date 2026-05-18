package com.maxot.seekandcatch.feature.settings

import com.maxot.seekandcatch.core.media.AudioManager
import com.maxot.seekandcatch.core.media.MusicType
import com.maxot.seekandcatch.core.media.SoundType

class AudioControllerImpl(
    private val audioManager: AudioManager
) : AudioController {
    override fun onButtonClick() = audioManager.onButtonClick()
    override fun onMusicSettingChanged(enabled: Boolean) = audioManager.onMusicSettingChanged(enabled)
    override fun onGameStart() = audioManager.onGameStart()
    override fun onGameplayStarted() = audioManager.onGameplayStarted()
    override fun onGamePaused() = audioManager.onGamePaused()
    override fun onGameResumed() = audioManager.onGameResumed()
    override fun onGameOver() = audioManager.onGameOver()
    override fun onCorrectTap() = audioManager.onCorrectTap()
    override fun onMiss() = audioManager.onMiss()
    override fun playMenuMusic() = audioManager.playMusic(MusicType.MENU)
    override fun pauseMusic() = audioManager.pauseMusic()
    override fun resumeMusic() = audioManager.resumeMusic()
    override fun stopMusic() = audioManager.stopMusic()
    override fun release() = audioManager.release()
    override fun playNewBestScore() = audioManager.playSound(SoundType.NEW_BEST_SCORE)
}
