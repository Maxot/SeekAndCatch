package com.maxot.seekandcatch.core.media

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.maxot.seekandcatch.core.media.provider.SettingsProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

enum class SoundType(val resId: Int) {
    FIGURE_CLICK(R.raw.figure_click),
    BUTTON_CLICK(R.raw.button_click),
    COUNTDOWN(R.raw.bit_countdown),
    GAME_OVER(R.raw.game_over)
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

    fun playSound(soundType: SoundType) {
        CoroutineScope(Dispatchers.Default).launch {
            if (settingsProvider.isSoundEnabled()) {
                soundMap[soundType]?.let { soundId ->
                    soundPool.play(soundId, 0.25f, 0.25f, 1, 0, 1f)
                } ?: run {
                    android.util.Log.w("SoundManager", "Sound not loaded: $soundType")
                }
            }
        }
    }

    fun release() {
        soundPool.release()
    }
}
