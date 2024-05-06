package com.maxot.seekandcatch.feature.settings

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import com.maxot.seekandcatch.data.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class MusicManager
@Inject constructor(
    @ApplicationContext val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private var mediaPlayer: MediaPlayer? = null

    init {
        CoroutineScope(Job()).launch {
            val isMusicEnabled = settingsRepository.observeMusicState().first()
            if (isMusicEnabled) {
                mediaPlayer = MediaPlayer.create(context, R.raw.game_sound).apply {
                    isLooping = true
                }
            }
            this.cancel()
        }
    }

    fun startMusic() {
        mediaPlayer?.start()
    }

    fun pauseMusic() {
        mediaPlayer?.pause()
    }

    fun stopMusic() {
        mediaPlayer?.stop()
    }

    fun release() {
        mediaPlayer?.release()
    }

    fun setMusicSpeed(newSpeed: Float) {
        mediaPlayer?.playbackParams = PlaybackParams().apply {
            speed = newSpeed
        }
    }

}
