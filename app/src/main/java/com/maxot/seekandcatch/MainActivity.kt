package com.maxot.seekandcatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.ui.SeekAndCatchApp
import com.maxot.seekandcatch.ui.SeekAndCatchAppState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()
            val appState = SeekAndCatchAppState(navController, coroutineScope)

            SeekAndCatchTheme {
                enableEdgeToEdge()
                SeekAndCatchApp(appState = appState)
            }
        }
    }
}
