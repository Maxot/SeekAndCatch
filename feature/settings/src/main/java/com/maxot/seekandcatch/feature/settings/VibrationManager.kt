package com.maxot.seekandcatch.feature.settings

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.maxot.seekandcatch.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class VibrationManager(
    val context: Context,
    private val settingsRepository: SettingsRepository
) : HapticsController {
    private lateinit var vibratorManager: VibratorManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        }
    }

    override suspend fun vibrateCorrect() {
        vibrateEffect(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            } else {
                VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
            }
        )
    }

    override suspend fun vibrateError() {
        val pattern = longArrayOf(0, 100, 50, 100) // Double pulse
        vibrateEffect(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect.createWaveform(pattern, -1)
            } else {
                null // Fallback to deprecated vibrate if needed, but here we use createOneShot if waveform not available
            }
        , pattern)
    }

    private suspend fun vibrateEffect(effect: VibrationEffect?, pattern: LongArray? = null) {
        if (settingsRepository.observeVibrationState().first()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val combinedVibration = if (effect != null) {
                    CombinedVibration.createParallel(effect)
                } else {
                    return
                }
                vibratorManager.vibrate(combinedVibration)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (vibrator.hasVibrator()) {
                    if (effect != null) {
                        vibrator.vibrate(effect)
                    } else if (pattern != null) {
                        vibrator.vibrate(pattern, -1)
                    }
                }
            }
        }
    }

    override suspend fun vibrate(duration: Long) {
        vibrateEffect(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
