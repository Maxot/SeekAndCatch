package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.designsystem.ui.PixelBorderBox
import com.maxot.seekandcatch.core.designsystem.ui.PixelButton
import com.maxot.seekandcatch.feature.gameplay.R

@Composable
fun PauseDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String
) {
    val pauseDialogContentDesc = stringResource(id = R.string.pause_dialog_content_desc)
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
                Column {
                    Text(text = dialogTitle)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = dialogText)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        PixelButton(onClick = { onConfirmation() }) {
                            Text(
                                text = stringResource(id = R.string.confirm_button_pause_dialog),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        PixelButton(onClick = { onDismissRequest() }) {
                            Text(
                                text = stringResource(id = R.string.dismiss_button_pause_dialog),
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

@Preview
@Composable
fun PauseDialogPreview() {
    SeekAndCatchTheme {
        PauseDialog(
            onDismissRequest = { /*TODO*/ },
            onConfirmation = { /*TODO*/ },
            dialogTitle = "Hello",
            dialogText = "World"
        )
    }
}