package com.maxot.seekandcatch.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.core.common.model.User
import com.maxot.seekandcatch.core.designsystem.R
import com.maxot.seekandcatch.core.designsystem.icon.SaCIcons
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme

@Composable
fun UserInfoPanel(
    modifier: Modifier = Modifier,
    user: User,
    onUserNameChanged: (userName: String) -> Unit
) {
    var editText by rememberSaveable { mutableStateOf("") }
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var hasFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(isEditing) {
        if (isEditing) focusRequester.requestFocus()
    }

    fun confirmEdit() {
        if (editText.isNotEmpty()) onUserNameChanged(editText)
        isEditing = false
        keyboardController?.hide()
    }

    fun cancelEdit() {
        isEditing = false
        keyboardController?.hide()
    }

    PixelBorderBox(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = stringResource(R.string.feature_account_your_info),
                modifier = Modifier.padding(5.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.feature_account_user_id_title),
                    style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.padding(5.dp)
                )
                Text(
                    text = user.id,
                    style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onSecondary),
                    modifier = Modifier.weight(1f).padding(5.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = SaCIcons.Copy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .padding(5.dp)
                        .clickable { clipboardManager.setText(AnnotatedString(user.id)) }
                )
            }

            if (isEditing) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { state ->
                            if (state.isFocused) hasFocused = true
                            else if (hasFocused) cancelEdit()
                        },
                    value = editText,
                    onValueChange = { editText = it },
                    label = { Text(stringResource(id = R.string.feature_account_user_name_title)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { confirmEdit() })
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            hasFocused = false
                            editText = ""
                            isEditing = true
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.feature_account_user_name_title),
                        style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.padding(5.dp)
                    )
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onSecondary),
                        modifier = Modifier.weight(1f).padding(5.dp)
                    )
                    Icon(
                        imageVector = SaCIcons.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun UserInfoPanelPreview() {
    SeekAndCatchTheme {
        UserInfoPanel(
            user = User(
                id = "user id",
                name = "Player_3F9A"
            ),
            onUserNameChanged = {}
        )
    }
}
