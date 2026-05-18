package com.maxot.seekandcatch.feature.settings

interface AudioController {
    fun onButtonClick()
    fun onMusicSettingChanged(enabled: Boolean)
    fun onGameStart()
    fun onGameplayStarted()
    fun onGamePaused()
    fun onGameResumed()
    fun onGameOver()
    fun onCorrectTap()
    fun onMiss()
    fun playMenuMusic()
    fun pauseMusic()
    fun resumeMusic()
    fun stopMusic()
    fun release()
    fun playNewBestScore()
}
