package com.maxot.seekandcatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maxot.seekandcatch.ui.navigation.Navigation
import com.maxot.seekandcatch.ui.theme.SeekAndCatchTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeekAndCatchTheme {
                val viewModel = viewModel<GameViewModel>()
                Navigation(viewModel = viewModel)
            }
        }
    }
}
