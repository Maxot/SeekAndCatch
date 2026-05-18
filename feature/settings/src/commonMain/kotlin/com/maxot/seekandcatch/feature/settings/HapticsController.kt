package com.maxot.seekandcatch.feature.settings

interface HapticsController {
    suspend fun vibrate(duration: Long = 250)
    suspend fun vibrateCorrect()
    suspend fun vibrateError()
}
