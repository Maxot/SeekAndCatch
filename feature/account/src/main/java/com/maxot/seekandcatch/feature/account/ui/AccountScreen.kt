package com.maxot.seekandcatch.feature.account.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maxot.seekandcatch.core.common.model.Figure
import com.maxot.seekandcatch.core.common.model.User
import com.maxot.seekandcatch.core.designsystem.component.PixelBorderBox
import com.maxot.seekandcatch.core.designsystem.component.PixelButton
import com.maxot.seekandcatch.core.designsystem.component.UserInfoPanel
import com.maxot.seekandcatch.core.designsystem.component.drawCircleFigure
import com.maxot.seekandcatch.core.designsystem.component.drawSquareFigure
import com.maxot.seekandcatch.core.designsystem.component.drawTriangleFigure
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.feature.account.AccountViewModel
import com.maxot.seekandcatch.feature.account.R
import com.maxot.seekandcatch.feature.account.ui.model.AccountScreenEvent
import com.maxot.seekandcatch.feature.colorpicker.ColorPicker

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    AccountScreenContent(
        modifier = modifier,
        user = uiState.value.user ?: User("unknow user"),
        onUserNameChanged = {
            viewModel.onEvent(AccountScreenEvent.ChangeName(it))
        },
        availableColors = uiState.value.availableColors,
        selectedColors = uiState.value.selectedColors,
        onSelectedColorsChanged = {
            viewModel.onEvent(AccountScreenEvent.ChangeSelectedColors(it))
        },
        onDeleteAccount = { viewModel.onEvent(AccountScreenEvent.DeleteAccount) }
    )
}

@Composable
private fun AccountScreenContent(
    modifier: Modifier = Modifier,
    user: User,
    onUserNameChanged: (userName: String) -> Unit,
    availableColors: Set<Color>,
    selectedColors: Set<Color>,
    onSelectedColorsChanged: (Set<Color>) -> Unit,
    onDeleteAccount: () -> Unit = {},
) {
    val contentDesc = stringResource(id = R.string.feature_account_screen_content_desc)
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            containerColor = Color.Transparent,
            title = null,
            text = {
                PixelBorderBox {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.feature_account_delete_confirm_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFD6D68D)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.feature_account_delete_confirm_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD6D68D)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        PixelButton(
                            modifier = Modifier.fillMaxWidth(),
                            paddingValues = PaddingValues(10.dp),
                            onClick = {
                                showDeleteDialog = false
                                onDeleteAccount()
                            }
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                text = stringResource(R.string.feature_account_delete_confirm_button),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF1A3B20)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        PixelButton(
                            modifier = Modifier.fillMaxWidth(),
                            paddingValues = PaddingValues(10.dp),
                            onClick = { showDeleteDialog = false }
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                text = stringResource(R.string.feature_account_cancel),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF1A3B20)
                            )
                        }
                    }
                }
            },
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {}
        )
    }

    Column(
        modifier = modifier
            .then(modifier)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .semantics {
                contentDescription = contentDesc
            },
        verticalArrangement = Arrangement.Top
    ) {
        UserInfoPanel(
            modifier = Modifier.fillMaxWidth(),
            user = user,
            onUserNameChanged = onUserNameChanged
        )

        StyleField(
            modifier = Modifier.padding(5.dp),
        )
        ColorsField(
            modifier = Modifier.padding(5.dp),
            availableColors = availableColors,
            selectedColors = selectedColors,
            onSelectedColorsChanged = onSelectedColorsChanged
        )

        Spacer(modifier = Modifier.height(16.dp))

        PixelButton(
            modifier = Modifier.fillMaxWidth(),
            paddingValues = PaddingValues(10.dp),
            onClick = { showDeleteDialog = true }
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
                text = stringResource(R.string.feature_account_delete_account),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1A3B20)
            )
        }
    }
}

@Composable
private fun ColorsField(
    modifier: Modifier = Modifier,
    availableColors: Set<Color>,
    selectedColors: Set<Color>,
    onSelectedColorsChanged: (Set<Color>) -> Unit
) {
    PixelBorderBox(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = stringResource(R.string.feature_account_selected_colors),
                modifier = Modifier
                    .padding(5.dp)
            )
            Row(
                modifier = Modifier
                    .padding(5.dp),
            ) {
                selectedColors.forEach { color ->
                    key(color.value) {
                        val possibleColors = availableColors - selectedColors + color
                        ColorPicker(
                            modifier = Modifier.padding(5.dp),
                            availableColors = possibleColors,
                            selectedColor = color
                        ) { newColor ->
                            val currentlySelectedColor = selectedColors.toMutableList()
                            val currentColorIndex = currentlySelectedColor.indexOf(color)
                            currentlySelectedColor[currentColorIndex] = newColor

                            onSelectedColorsChanged(currentlySelectedColor.toSet())

                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun StyleField(
    modifier: Modifier = Modifier
) {
    PixelBorderBox(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = stringResource(R.string.feature_account_selected_style),
                modifier = Modifier
                    .padding(5.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Figure.FigureType.entries.forEach {
                    Box {
                        Canvas(modifier = Modifier.size(50.dp)) {
                            val sizePx = this.size.minDimension

                            when (it) {
                                Figure.FigureType.SQUARE -> drawSquareFigure(sizePx, Color.Red)
                                Figure.FigureType.CIRCLE -> drawCircleFigure(sizePx, Color.Blue)
                                Figure.FigureType.TRIANGLE -> drawTriangleFigure(
                                    sizePx,
                                    Color.Green
                                )
                            }
                        }
                    }
                }

            }
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun AccountScreenPreview() {
    SeekAndCatchTheme {
        AccountScreenContent(
            user = User(
                id = "userId",
                name = "userName"
            ),
            onUserNameChanged = {},
            availableColors = setOf(
                Color.Red,
                Color.Blue,
                Color.Green,
                Color.Yellow,
                Color.Magenta
            ),
            selectedColors = setOf(Color.Red, Color.Blue, Color.Green, Color.Yellow),
            onSelectedColorsChanged = {},
            onDeleteAccount = {}
        )
    }
}
