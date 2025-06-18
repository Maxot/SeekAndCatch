package com.maxot.seekandcatch.core.media

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.maxot.seekandcatch.core.media.provider.SettingsProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class SoundType(val resId: Int) {
    FIGURE_CLICK(R.raw.figure_click),
    BUTTON_CLICK(R.raw.button_click),
//    GAME_START(R.raw.game_start),
//    GAME_OVER(R.raw.game_over)
}

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsProvider: SettingsProvider
) {
    private val soundPool: SoundPool
    private val soundMap: MutableMap<SoundType, Int> = mutableMapOf()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Automatically load all sounds defined in SoundType
        SoundType.entries.forEach { soundType ->
            val soundId = soundPool.load(context, soundType.resId, 1)
            soundMap[soundType] = soundId
        }
    }

    suspend fun playSound(soundType: SoundType) {
        if (settingsProvider.isSoundEnabled()) {
            soundMap[soundType]?.let { soundId ->
                soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            } ?: run {
                android.util.Log.w("SoundManager", "Sound not loaded: $soundType")
            }
        }
    }

    fun release() {
        soundPool.release()
    }
}
