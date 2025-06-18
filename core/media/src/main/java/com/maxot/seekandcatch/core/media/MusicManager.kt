package com.maxot.seekandcatch.core.media

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import com.maxot.seekandcatch.core.media.provider.SettingsProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

class MusicManager
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsProvider: SettingsProvider
) {

    private var mediaPlayer: MediaPlayer? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        // Launch coroutine to check music setting and initialize player if enabled
        scope.launch {
            val isMusicEnabled = settingsProvider.isMusicEnabled()
            if (isMusicEnabled) {
                mediaPlayer = MediaPlayer.create(context, R.raw.game_music).apply {
                    isLooping = true
                }
            }
        }
    }

    fun startMusic() {
        mediaPlayer?.takeIf { !it.isPlaying }?.start()
    }

    fun pauseMusic() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
    }

    fun stopMusic() {
        mediaPlayer?.takeIf { it.isPlaying }?.stop()
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        scope.cancel()
    }

    fun setMusicSpeed(newSpeed: Float) {
        mediaPlayer?.playbackParams = PlaybackParams().apply {
            speed = newSpeed
        }
    }
}