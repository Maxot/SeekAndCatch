package com.maxot.seekandcatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.maxot.seekandcatch.ui.SeekAndCatchAppState
import com.maxot.seekandcatch.ui.navigation.SeekCatchNavHost
import com.maxot.seekandcatch.ui.theme.SeekAndCatchTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()
            val appState = SeekAndCatchAppState(navController, coroutineScope)

            val backgroundBrush =
                Brush.linearGradient(listOf(Color.Transparent, Color.Yellow, Color.Green, Color.Blue))

            SeekAndCatchTheme {
                Box(Modifier.background(backgroundBrush)) {
                    SeekCatchNavHost(appState = appState)
                }

            }
        }
    }
}
