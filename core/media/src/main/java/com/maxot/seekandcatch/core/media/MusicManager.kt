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

    private fun initializePlayer(musicType: MusicType) {
        scope.launch {
            if (settingsProvider.isMusicEnabled()) {
                _currentMusicType = musicType
                mediaPlayer = MediaPlayer.create(context, musicType.resId)?.apply {
                    isLooping = true
                    start()
                }
            }
        }
    }

    fun play(musicType: MusicType) {
        if (_currentMusicType == musicType) {
            mediaPlayer?.let {
                if (!it.isPlaying) it.start()
                return
            }
        }
        stopMusic()
        initializePlayer(musicType)
    }

    fun pauseMusic() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
    }

    fun stopMusic() {
        mediaPlayer?.takeIf { it.isPlaying }?.stop()
        _currentMusicType = null
    }

    fun resumeMusic() {
        mediaPlayer?.start()
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