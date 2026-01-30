package com.maxot.seekandcatch.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
    var text by rememberSaveable { mutableStateOf("") }
    var editTextEnabled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(key1 = user) {
        text = user.name
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
                modifier = Modifier
                    .padding(5.dp)
            )

            Row(
                modifier = Modifier
                    .then(modifier),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.feature_account_user_id_title),
                    style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .padding(5.dp),
                )

                Text(
                    text = user.id,
                    style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onSecondary),
                    modifier = Modifier
                        .padding(5.dp)
                )
            }

            Row(
                modifier = Modifier
                    .then(modifier),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    modifier = Modifier.weight(3f),
                    enabled = editTextEnabled,
                    value = text,
                    onValueChange = {
                        text = it
                    },
                    label = {
                        Text(stringResource(id = R.string.feature_account_user_name_title))
                    })

                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        editTextEnabled = !editTextEnabled
                        if (text.isNotEmpty() && !editTextEnabled)
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
}

@Preview
@Composable
private fun UserInfoPanelPreview() {
    SeekAndCatchTheme {
        UserInfoPanel(
            user = User(
                id = "user id",
                name = "user name"
            ),
            onUserNameChanged = {}
        )
    }
}