package com.maxot.seekandcatch.feature.account.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maxot.seekandcatch.core.designsystem.icon.SaCIcons
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.feature.account.AccountViewModel
import com.maxot.seekandcatch.feature.account.R


@Composable
fun AccountScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle("")

    AccountScreen(
        modifier = modifier,
        userName = userName,
        onUserNameChanged = viewModel::setUserName
    )
}

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    userName: String,
    onUserNameChanged: (userName: String) -> Unit
) {
    val contentDesc = stringResource(id = R.string.feature_account_screen_content_desc)

    var text by rememberSaveable { mutableStateOf("") }
    var editTextEnabled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(key1 = userName) {
        text = userName
    }

    Column(
        modifier = modifier
            .then(modifier)
            .fillMaxSize()
            .semantics {
                contentDescription = contentDesc
            },
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
                    Text(stringResource(id = R.string.feature_account_user_name_title))
                })
            IconButton(onClick = {
                editTextEnabled = !editTextEnabled
                if (text.isNotEmpty())
                    onUserNameChanged(text)
            }) {
                Icon(
                    imageVector = if (editTextEnabled) SaCIcons.Done else SaCIcons.Edit,
                    contentDescription = ""
                )
            }
        }
    }
}

@Preview
@Composable
fun AccountScreenPreview() {
    SeekAndCatchTheme {
        AccountScreen(userName = "User name") {

        }
    }
}