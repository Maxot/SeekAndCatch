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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.compose.rememberNavController
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.media.MusicManager
import com.maxot.seekandcatch.core.media.di.rememberMusicManager
import com.maxot.seekandcatch.core.model.DarkThemeConfig
import com.maxot.seekandcatch.ui.SeekAndCatchApp
import com.maxot.seekandcatch.ui.SeekAndCatchAppState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var musicController: MusicController

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(musicController)
        setContent {
            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()
            val viewModel = hiltViewModel<MainViewModel>()
            val musicManager = rememberMusicManager()
            val appState = SeekAndCatchAppState(navController, coroutineScope, musicManager)
            val uiState = viewModel.uiState.collectAsState()

            SeekAndCatchTheme(darkTheme = isDarkTheme(uiState.value)) {
                enableEdgeToEdge()
                appState.ObserveMusicByDestination()
                SeekAndCatchApp(appState = appState)
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

class MusicController @Inject constructor(
    private val musicManager: MusicManager
) : DefaultLifecycleObserver {
    override fun onPause(owner: LifecycleOwner) {
        musicManager.pauseMusic()
    }

    override fun onResume(owner: LifecycleOwner) {
        musicManager.resumeMusic()
    }

    override fun onStop(owner: LifecycleOwner) {
        musicManager.stopMusic()
    }
}
