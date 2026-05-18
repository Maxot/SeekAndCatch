package com.maxot.seekandcatch.core.media.di

import androidx.compose.runtime.Composable
import com.maxot.seekandcatch.core.media.AudioManager
import org.koin.compose.koinInject

@Composable
fun rememberAudioManager(): AudioManager = koinInject()
