package com.maxot.seekandcatch.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.feature.settings.R
import com.maxot.seekandcatch.feature.settings.SettingsViewModel

@Composable
fun SettingsDialog(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
) {
    val isSoundEnabled by viewModel.soundState.collectAsState(false)

    SettingsDialog(
        modifier = modifier,
        isSoundEnabled = isSoundEnabled,
        onDismiss = onDismiss,
        onConfirmation = onDismiss,
        onSoundStateChanged = viewModel::setSoundState
    )
}

@Composable
fun SettingsDialog(
    modifier: Modifier = Modifier,
    isSoundEnabled: Boolean,
    onDismiss: () -> Unit,
    onConfirmation: () -> Unit,
    onSoundStateChanged: (soundEnabled: Boolean) -> Unit
) {
    val settingsDialogContentDesc =
        stringResource(id = R.string.feature_settings_dialog_content_description)

    AlertDialog(
        modifier = Modifier
            .then(modifier)
            .semantics {
                contentDescription = settingsDialogContentDesc
            },
        title = {
            Text(text = stringResource(id = R.string.feature_settings_title))
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(id = R.string.feature_settings_sounds_title))
                Switch(checked = isSoundEnabled, onCheckedChange = {
                    onSoundStateChanged(it)
                })
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirmation() }) {
                Text(text = stringResource(id = R.string.feature_settings_ok))
            }
        }
    )
}

@Preview
@Composable
fun SettingsDialogPreview() {
    SeekAndCatchTheme {
        SettingsDialog(
            isSoundEnabled = true,
            onDismiss = { },
            onConfirmation = { },
            onSoundStateChanged = { }
        )
    }
}