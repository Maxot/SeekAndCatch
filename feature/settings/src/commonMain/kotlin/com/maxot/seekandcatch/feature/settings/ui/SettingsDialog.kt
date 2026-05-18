package com.maxot.seekandcatch.feature.settings.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxot.seekandcatch.core.designsystem.icon.SaCIcons
import org.koin.compose.viewmodel.koinViewModel
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.designsystem.component.PixelBorderBox
import com.maxot.seekandcatch.core.designsystem.component.PixelButton
import com.maxot.seekandcatch.core.designsystem.component.PixelToggle
import com.maxot.singleselectionlazyrow.ScaleParams
import com.maxot.singleselectionlazyrow.SingleSelectionLazyRow
import com.maxot.seekandcatch.feature.settings.generated.resources.Res
import com.maxot.seekandcatch.feature.settings.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsDialog(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
    onDismiss: () -> Unit,
) {
    val isSoundEnabled by viewModel.soundState.collectAsState(true)
    val isMusicEnabled by viewModel.musicState.collectAsState(true)
    val isVibrationEnabled by viewModel.vibrationState.collectAsState(true)
    val allSupportedLocales by remember { mutableStateOf(viewModel.allSupportedLocales) }
    val selectedLocale by remember { mutableStateOf(viewModel.selectedLocale) }
    val darkTheme by viewModel.darkTheme.collectAsState()
    val isColorblindModeEnabled by viewModel.colorblindMode.collectAsState(false)

    SettingsDialog(
        modifier = modifier,
        allSupportedLocales = allSupportedLocales,
        selectedLocale = selectedLocale,
        isSoundEnabled = isSoundEnabled,
        isMusicEnabled = isMusicEnabled,
        isVibrationEnabled = isVibrationEnabled,
        isColorblindModeEnabled = isColorblindModeEnabled,
        onDismiss = onDismiss,
        onConfirmation = onDismiss,
        onSoundStateChanged = viewModel::setSoundState,
        onMusicStateChanged = viewModel::setMusicState,
        onVibrationStateChanged = viewModel::setVibrationState,
        onColorblindModeChanged = viewModel::setColorblindModeEnabled,
        onLocaleChanged = viewModel::updateSelectedLocale,
        darkTheme = darkTheme,
        onDarkThemeChanged = {
            viewModel.setDarkTheme(it)
        }

    )
}

@Composable
private fun SettingsDialog(
    modifier: Modifier = Modifier,
    allSupportedLocales: List<String>,
    selectedLocale: String,
    isSoundEnabled: Boolean,
    isMusicEnabled: Boolean,
    isVibrationEnabled: Boolean,
    isColorblindModeEnabled: Boolean,
    onDismiss: () -> Unit,
    onConfirmation: () -> Unit,
    onSoundStateChanged: (Boolean) -> Unit,
    onMusicStateChanged: (Boolean) -> Unit,
    onVibrationStateChanged: (Boolean) -> Unit,
    onColorblindModeChanged: (Boolean) -> Unit,
    onLocaleChanged: (String) -> Unit,
    darkTheme: Boolean,
    onDarkThemeChanged: (Boolean) -> Unit
) {
    val settingsDialogContentDesc =
        stringResource(Res.string.feature_settings_dialog_content_description)

    AlertDialog(
        modifier = modifier.semantics {
            contentDescription = settingsDialogContentDesc
        },
        containerColor = Color.Transparent,
        title = null,
        text = {
            PixelBorderBox {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(Res.string.feature_settings_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFD6D68D)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsPanel(
                        allSupportedLocales = allSupportedLocales,
                        selectedLocale = selectedLocale,
                        isSoundEnabled = isSoundEnabled,
                        isMusicEnabled = isMusicEnabled,
                        isVibrationEnabled = isVibrationEnabled,
                        isColorblindModeEnabled = isColorblindModeEnabled,
                        darkTheme = darkTheme,
                        onSoundStateChanged = onSoundStateChanged,
                        onMusicStateChanged = onMusicStateChanged,
                        onVibrationStateChanged = onVibrationStateChanged,
                        onColorblindModeChanged = onColorblindModeChanged,
                        onLocaleChanged = onLocaleChanged,
                        onDarkThemeChanged = onDarkThemeChanged
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PixelButton(
                        paddingValues = PaddingValues(10.dp),
                        onClick = onConfirmation
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
                            text = stringResource(Res.string.feature_settings_ok),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1A3B20)
                        )
                    }
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {} // ми замінили на свою кнопку всередині `text`
    )
}

@Composable
private fun SettingsPanel(
    modifier: Modifier = Modifier,
    isSoundEnabled: Boolean,
    isMusicEnabled: Boolean,
    isVibrationEnabled: Boolean,
    isColorblindModeEnabled: Boolean,
    darkTheme: Boolean,
    allSupportedLocales: List<String>,
    selectedLocale: String,
    onSoundStateChanged: (Boolean) -> Unit,
    onMusicStateChanged: (Boolean) -> Unit,
    onVibrationStateChanged: (Boolean) -> Unit,
    onColorblindModeChanged: (Boolean) -> Unit,
    onDarkThemeChanged: (Boolean) -> Unit,
    onLocaleChanged: (String) -> Unit
) {
    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LanguagesPanel(
                allSupportedLocales = allSupportedLocales,
                selectedLocale = selectedLocale,
                onLocaleChanged = onLocaleChanged
            )

            PixelSettingRow(
                iconRes = SaCIcons.Sounds,
                label = stringResource(Res.string.feature_settings_dark_theme_title),
                checked = darkTheme,
                onCheckedChange = onDarkThemeChanged
            )

            PixelSettingRow(
                iconRes = SaCIcons.Settings,
                label = stringResource(Res.string.feature_settings_colorblind_title),
                checked = isColorblindModeEnabled,
                onCheckedChange = onColorblindModeChanged
            )

            PixelSettingRow(
                iconRes = SaCIcons.Sounds,
                label = stringResource(Res.string.feature_settings_sound_title),
                checked = isSoundEnabled,
                onCheckedChange = onSoundStateChanged
            )

            PixelSettingRow(
                iconRes = SaCIcons.Music,
                label = stringResource(Res.string.feature_settings_music_title),
                checked = isMusicEnabled,
                onCheckedChange = onMusicStateChanged
            )

            PixelSettingRow(
                iconRes = SaCIcons.Vibration,
                label = stringResource(Res.string.feature_settings_vibration_title),
                checked = isVibrationEnabled,
                onCheckedChange = onVibrationStateChanged
            )
        }
    }
}

@Composable
private fun PixelSettingRow(
    iconRes: DrawableResource,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.padding(end = 10.dp),
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Color(0xFFD6D68D)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFFD6D68D),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        PixelToggle(
            isOn = checked,
            onToggle = { onCheckedChange(!checked) }
        )
    }
}

@Composable
private fun LanguagesPanel(
    modifier: Modifier = Modifier,
    allSupportedLocales: List<String> = listOf("en-US", "uk"),
    selectedLocale: String,
    onLocaleChanged: (String) -> Unit,
) {
    SingleSelectionLazyRow(
        items = allSupportedLocales,
        scaleParams = ScaleParams(scale1 = 1.1f, scale2 = 0.4f),
        selectedItemIndex = allSupportedLocales.indexOf(selectedLocale),
        onSelectedItemChanged = { item ->
            onLocaleChanged(allSupportedLocales[item])
        },
    ) { modifier, selectedItem ->
        LanguageLayout(modifier, selectedItem)
    }
}

@Composable
private fun LanguageLayout(modifier: Modifier, language: String) {
    Box(
        modifier = Modifier
            .then(modifier)
            .size(100.dp)
            .padding(20.dp)
    ) {
        val icon = when (language) {
            "uk" -> Res.drawable.ic_ukraine_flag
            "en" -> Res.drawable.ic_united_kingdom_flag
            else -> Res.drawable.ic_united_kingdom_flag
        }
        Image(
            painter = painterResource(icon),
            contentDescription = "",
            modifier = Modifier.fillMaxSize()
        )
    }
}
