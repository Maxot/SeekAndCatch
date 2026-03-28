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
import javax.inject.Singleton

enum class MusicType(val resId: Int) {
    GAME(R.raw.game_music),
    MENU(R.raw.menu_music)
}

@Singleton
class MusicManager
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsProvider: SettingsProvider
) {
    private var mediaPlayer: MediaPlayer? = null

    private var _currentMusicType: MusicType? = null
    val currentMusicType: MusicType?
        get() = _currentMusicType

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun play(musicType: MusicType) {
        _currentMusicType = musicType
        scope.launch {
            if (settingsProvider.isMusicEnabled()) {
                if (mediaPlayer?.isPlaying == true && _currentMusicType == musicType) {
                    return@launch
                }
                initializePlayer(musicType)
            }
        }
    }

    private fun initializePlayer(musicType: MusicType) {
        _currentMusicType = musicType
        if (mediaPlayer != null) {
            stopMusicInternal()
        }
        mediaPlayer = MediaPlayer.create(context, musicType.resId)?.apply {
            isLooping = true
            start()
        }
    }

    fun onMusicSettingChanged(enabled: Boolean) {
        if (enabled) {
            val type = _currentMusicType
            if (type != null && mediaPlayer == null) {
                initializePlayer(type)
            }
        } else {
            stopMusicInternal()
        }
    }

    fun pauseMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) it.pause()
        }
    }

    fun stopMusic() {
        _currentMusicType = null
        stopMusicInternal()
    }

    private fun stopMusicInternal() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
            } catch (e: Exception) {
                // Ignore
            }
            it.release()
        }
        mediaPlayer = null
    }

    fun resumeMusic() {
        scope.launch {
            if (settingsProvider.isMusicEnabled()) {
                if (mediaPlayer != null) {
                    mediaPlayer?.start()
                } else {
                    val type = _currentMusicType
                    if (type != null) {
                        initializePlayer(type)
                    }
                }
            }
        }
    }

    fun releaseMusic() {
        mediaPlayer?.release()
        mediaPlayer = null
        _currentMusicType = null
        scope.cancel()
    }

    fun setMusicSpeed(newSpeed: Float) {
        mediaPlayer?.playbackParams = PlaybackParams().apply {
            speed = newSpeed
        }
    }
}