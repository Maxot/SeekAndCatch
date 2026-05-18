package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.designsystem.component.PixelBorderBox
import com.maxot.seekandcatch.core.designsystem.component.PixelButton
import com.maxot.seekandcatch.core.designsystem.component.PixelToggle
import com.maxot.seekandcatch.core.designsystem.icon.SaCIcons
import com.maxot.seekandcatch.feature.gameplay.generated.resources.Res
import com.maxot.seekandcatch.feature.gameplay.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PauseDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    isSoundEnabled: Boolean = true,
    isMusicEnabled: Boolean = true,
    isVibrationEnabled: Boolean = true,
    onSoundToggle: (Boolean) -> Unit = {},
    onMusicToggle: (Boolean) -> Unit = {},
    onVibrationToggle: (Boolean) -> Unit = {}
) {
    val pauseDialogContentDesc = stringResource(Res.string.pause_dialog_content_desc)
    AlertDialog(
        modifier = Modifier
            .then(modifier)
            .semantics {
                contentDescription = pauseDialogContentDesc
            },
        containerColor = Color.Transparent,
        onDismissRequest = onDismissRequest,
        text = {
            PixelBorderBox {
                Column(
                    modifier = Modifier.padding(15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = dialogTitle)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = dialogText)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuickSettingsToggle(
                            iconRes = SaCIcons.Sounds,
                            checked = isSoundEnabled,
                            onCheckedChange = onSoundToggle
                        )
                        QuickSettingsToggle(
                            iconRes = SaCIcons.Music,
                            checked = isMusicEnabled,
                            onCheckedChange = onMusicToggle
                        )
                        QuickSettingsToggle(
                            iconRes = SaCIcons.Vibration,
                            checked = isVibrationEnabled,
                            onCheckedChange = onVibrationToggle
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        PixelButton(
                            paddingValues = PaddingValues(5.dp),
                            onClick = { onConfirmation() }) {
                            Text(
                                modifier = Modifier.padding(20.dp),
                                text = stringResource(Res.string.confirm_button_pause_dialog),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        PixelButton(
                            paddingValues = PaddingValues(5.dp),
                            onClick = { onDismissRequest() }) {
                            Text(
                                modifier = Modifier.padding(20.dp),
                                maxLines = 1,
                                text = stringResource(Res.string.dismiss_button_pause_dialog),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
private fun QuickSettingsToggle(
    iconRes: DrawableResource,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.5f
            ),
            modifier = Modifier.size(24.dp)
        )
        PixelToggle(
            isOn = checked,
            onToggle = { onCheckedChange(!checked) }
        )
    }
}
