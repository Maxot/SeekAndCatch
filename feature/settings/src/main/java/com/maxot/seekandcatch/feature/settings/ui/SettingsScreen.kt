package com.maxot.seekandcatch.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maxot.seekandcatch.core.designsystem.icon.SaCIcons
import com.maxot.seekandcatch.feature.settings.SettingsViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {

    val isSoundEnabled = viewModel.soundState.collectAsState(false)
    val userName = viewModel.userName.collectAsStateWithLifecycle("")

    var text by rememberSaveable { mutableStateOf("") }
    var editTextEnabled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(key1 = userName.value) {
        text = userName.value
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                enabled = editTextEnabled,
                value = text,
                onValueChange = {
                    text = it
                },
                label = {
                    Text("User Name")
                })
            IconButton(onClick = {
                editTextEnabled = !editTextEnabled
                if (text.isNotEmpty())
                    viewModel.setUserName(text)
            }) {
                Icon(
                    imageVector = if (editTextEnabled) SaCIcons.Done else SaCIcons.Edit,
                    contentDescription = ""
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sounds")
            Switch(checked = isSoundEnabled.value, onCheckedChange = {
                viewModel.setSoundState(it)
            })
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Text(text = "Version 0.0.1", textAlign = TextAlign.Center, modifier = Modifier)
    }

}
