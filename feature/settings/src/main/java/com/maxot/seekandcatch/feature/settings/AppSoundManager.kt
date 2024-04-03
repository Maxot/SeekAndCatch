package com.maxot.seekandcatch.feature.settings

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import com.maxot.seekandcatch.feature.settings.data.SettingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSoundManager
@Inject constructor(
    @ApplicationContext val context: Context,
    private val settingsDataStore: SettingsDataStore
) {
    private var mediaPlayer: MediaPlayer? = null
    suspend fun init() {
        settingsDataStore.soundStateFlow.collect { isSoundEnabled ->
            if (isSoundEnabled) {
                mediaPlayer = MediaPlayer.create(context, R.raw.game_sound).apply {
                    isLooping = true
                }
            } else {
                mediaPlayer = null
            }
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
