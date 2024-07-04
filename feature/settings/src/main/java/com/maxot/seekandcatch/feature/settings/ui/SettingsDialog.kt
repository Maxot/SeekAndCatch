package com.maxot.seekandcatch.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxot.seekandcatch.core.designsystem.icon.SaCIcons
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.designsystem.theme.Shapes
import com.maxot.seekandcatch.feature.settings.R
import com.maxot.seekandcatch.feature.settings.SettingsViewModel

@Composable
fun SettingsDialog(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
) {
    val isSoundEnabled by viewModel.soundState.collectAsState(true)
    val isMusicEnabled by viewModel.musicState.collectAsState(true)
    val isVibrationEnabled by viewModel.vibrationState.collectAsState(true)

    SettingsDialog(
        modifier = modifier,
        isSoundEnabled = isSoundEnabled,
        isMusicEnabled = isMusicEnabled,
        isVibrationEnabled = isVibrationEnabled,
        onDismiss = onDismiss,
        onConfirmation = onDismiss,
        onSoundStateChanged = viewModel::setSoundState,
        onMusicStateChanged = viewModel::setMusicState,
        onVibrationStateChanged = viewModel::setVibrationState
    )
}

@Composable
fun SettingsDialog(
    modifier: Modifier = Modifier,
    isSoundEnabled: Boolean,
    isMusicEnabled: Boolean,
    isVibrationEnabled: Boolean,
    onDismiss: () -> Unit,
    onConfirmation: () -> Unit,
    onSoundStateChanged: (soundEnabled: Boolean) -> Unit,
    onMusicStateChanged: (soundEnabled: Boolean) -> Unit,
    onVibrationStateChanged: (soundEnabled: Boolean) -> Unit
) {
    val settingsDialogContentDesc =
        stringResource(id = R.string.feature_settings_dialog_content_description)

    AlertDialog(
        modifier = Modifier
            .then(modifier)
            .semantics {
                contentDescription = settingsDialogContentDesc
            },
        containerColor = MaterialTheme.colorScheme.background,
        title = {
            Text(text = stringResource(id = R.string.feature_settings_title))
        },
        text = {
            SettingsPanel(
                isSoundEnabled = isSoundEnabled,
                isMusicEnabled = isMusicEnabled,
                isVibrationEnabled = isVibrationEnabled,
                onSoundStateChanged = onSoundStateChanged,
                onMusicStateChanged = onMusicStateChanged,
                onVibrationStateChanged = onVibrationStateChanged
            )
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirmation() }) {
                Text(text = stringResource(id = R.string.feature_settings_ok))
            }
        }
    )
}

@Composable
fun SettingsPanel(
    modifier: Modifier = Modifier,
    isSoundEnabled: Boolean,
    isMusicEnabled: Boolean,
    isVibrationEnabled: Boolean,
    onSoundStateChanged: (soundEnabled: Boolean) -> Unit,
    onMusicStateChanged: (musicEnabled: Boolean) -> Unit,
    onVibrationStateChanged: (vibrationState: Boolean) -> Unit
) {
    Card(
        shape = Shapes.large,
        modifier = Modifier
            .then(modifier)
            .clip(Shapes.large),
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Icon(
                        modifier = Modifier.padding(end = 10.dp),
                        imageVector = ImageVector.vectorResource(SaCIcons.SoundsRes),
                        contentDescription = ""
                    )
                    Text(stringResource(id = R.string.feature_settings_sound_title))
                }
                Switch(checked = isSoundEnabled, onCheckedChange = {
                    onSoundStateChanged(it)
                })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Icon(
                        modifier = Modifier.padding(end = 10.dp),
                        imageVector = ImageVector.vectorResource(SaCIcons.MusicRes),
                        contentDescription = ""
                    )
                    Text(stringResource(id = R.string.feature_settings_music_title))
                }
                Switch(checked = isMusicEnabled, onCheckedChange = {
                    onMusicStateChanged(it)
                })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Icon(
                        modifier = Modifier.padding(end = 10.dp),
                        imageVector = ImageVector.vectorResource(SaCIcons.VibrationRes),
                        contentDescription = ""
                    )
                    Text(stringResource(id = R.string.feature_settings_vibration_title))
                }
                Switch(checked = isVibrationEnabled, onCheckedChange = {
                    onVibrationStateChanged(it)
                })
            }
        }
    }
}

@Preview
@Composable
fun SettingsDialogPreview() {
    SeekAndCatchTheme {
        SettingsDialog(
            isSoundEnabled = true,
            isMusicEnabled = true,
            isVibrationEnabled = true,
            onDismiss = { },
            onConfirmation = { },
            onSoundStateChanged = { },
            onMusicStateChanged = { },
            onVibrationStateChanged = { }
        )
    }
}