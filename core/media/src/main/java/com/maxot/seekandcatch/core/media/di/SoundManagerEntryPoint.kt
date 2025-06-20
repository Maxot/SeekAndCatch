package com.maxot.seekandcatch.core.media.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.maxot.seekandcatch.core.media.SoundManager
import com.maxot.seekandcatch.core.media.MusicManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SoundManagerEntryPoint {
    fun soundManager(): SoundManager
    fun musicManager(): MusicManager
}
@Composable
fun rememberSoundManager(): SoundManager {
    val context = LocalContext.current.applicationContext
    val entryPoint = EntryPointAccessors.fromApplication(
        context,
        SoundManagerEntryPoint::class.java
    )
    return entryPoint.soundManager()
}

@Composable
fun rememberMusicManager(): MusicManager {
    val context = LocalContext.current.applicationContext
    val entryPoint = EntryPointAccessors.fromApplication(
        context,
        SoundManagerEntryPoint::class.java
    )
    return entryPoint.musicManager()
}