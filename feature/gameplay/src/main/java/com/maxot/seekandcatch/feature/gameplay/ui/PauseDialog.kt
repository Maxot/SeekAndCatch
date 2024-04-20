package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
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
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onConfirmation() }) {
                Text(text = stringResource(id = R.string.confirm_button_pause_dialog))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(text = stringResource(id = R.string.dismiss_button_pause_dialog))
            }
        })
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