package com.maxot.seekandcatch

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.compose.rememberNavController
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.media.AudioManager
import com.maxot.seekandcatch.core.media.di.rememberAudioManager
import com.maxot.seekandcatch.core.model.DarkThemeConfig
import com.maxot.seekandcatch.ui.SeekAndCatchApp
import com.maxot.seekandcatch.ui.SeekAndCatchAppState
import org.koin.android.ext.android.inject
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : AppCompatActivity() {
    private val musicController: MusicController by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(musicController)
        setContent {
            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()
            val viewModel = koinViewModel<MainViewModel>()
            val audioManager = rememberAudioManager()
            val appState = SeekAndCatchAppState(navController, coroutineScope, audioManager)
            val uiState = viewModel.uiState.collectAsState()

            SeekAndCatchTheme(
                darkTheme = isDarkTheme(uiState.value),
                isColorblindModeEnabled = isColorblindModeEnabled(uiState.value)
            ) {
                enableEdgeToEdge()
                appState.ObserveMusicByDestination()
                SeekAndCatchApp(
                    appState = appState,
                    visualFeedbackManager = viewModel.visualFeedbackManager
                )
            }
        }
    }

}

@Composable
private fun isDarkTheme(uiState: MainActivityUiState): Boolean =
    when (uiState) {
        MainActivityUiState.Loading -> isSystemInDarkTheme()
        is MainActivityUiState.Success -> when (uiState.userConfig.darkThemeConfig) {
            DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
            DarkThemeConfig.LIGHT -> false
            DarkThemeConfig.DARK -> true
        }
    }

@Composable
private fun isColorblindModeEnabled(uiState: MainActivityUiState): Boolean =
    when (uiState) {
        MainActivityUiState.Loading -> false
        is MainActivityUiState.Success -> uiState.userConfig.isColorblindModeEnabled
    }

class MusicController(
    private val audioManager: AudioManager
) : DefaultLifecycleObserver {
    override fun onPause(owner: LifecycleOwner) {
        audioManager.pauseMusic()
    }

    override fun onResume(owner: LifecycleOwner) {
        audioManager.resumeMusic()
    }

    override fun onStop(owner: LifecycleOwner) {
        audioManager.stopMusic()
    }
}
