package com.maxot.seekandcatch.feature.settings

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.maxot.seekandcatch.data.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VibrationManager
@Inject constructor(
    @ApplicationContext val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private lateinit var vibratorManager: VibratorManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        }
    }

    suspend fun vibrate(duration: Long = 250) {
        if (settingsRepository.observeVibrationState().first()) {
            val vibrationEffect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val combinedVibration = CombinedVibration.createParallel(vibrationEffect)
                vibratorManager.vibrate(combinedVibration)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (vibrator.hasVibrator()) {
                    vibrator.vibrate(vibrationEffect)
                }
            }
        }
    }
}
