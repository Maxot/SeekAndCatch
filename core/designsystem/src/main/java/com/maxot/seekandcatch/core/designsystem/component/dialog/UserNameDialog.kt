package com.maxot.seekandcatch.core.designsystem.component.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.maxot.seekandcatch.core.designsystem.R
import com.maxot.seekandcatch.core.designsystem.component.UserNameField
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme

@Composable
fun UserNameDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    userName: String,
    updateUserName: (String) -> Unit,
) {
    AlertDialog(
        modifier = Modifier.then(modifier),
        title = {
            Text(text = stringResource(R.string.feature_account_enter_user_name))
        },
        text = {
            UserNameField(
                userName = userName,
                onUserNameChanged = updateUserName
            )
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                onConfirmation()
            }) {
                Text(text = "OK")
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun UserNameDialogPreview() {
    SeekAndCatchTheme {
        UserNameDialog(
            onDismissRequest = {},
            onConfirmation = {},
            userName = "name",
            updateUserName = {},
        )
    }
}
