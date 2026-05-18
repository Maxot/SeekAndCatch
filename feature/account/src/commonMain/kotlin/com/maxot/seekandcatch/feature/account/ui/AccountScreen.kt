package com.maxot.seekandcatch.feature.account.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.maxot.seekandcatch.core.common.model.User
import com.maxot.seekandcatch.core.designsystem.component.PixelBorderBox
import com.maxot.seekandcatch.core.designsystem.component.UserInfoPanel
import com.maxot.seekandcatch.core.designsystem.component.drawCircleFigure
import com.maxot.seekandcatch.core.designsystem.component.drawSquareFigure
import com.maxot.seekandcatch.core.designsystem.component.drawTriangleFigure
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.FigureColor
import com.maxot.seekandcatch.data.model.toComposeColor
import com.maxot.seekandcatch.data.model.toFigureColor
import com.maxot.seekandcatch.feature.account.AccountViewModel
import com.maxot.seekandcatch.feature.account.generated.resources.Res
import com.maxot.seekandcatch.feature.account.generated.resources.*
import com.maxot.seekandcatch.feature.account.ui.model.AccountScreenEvent
import com.maxot.seekandcatch.feature.colorpicker.ColorPicker

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = koinViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    AccountScreenContent(
        modifier = modifier,
        user = uiState.value.user ?: User("unknown user"),
        onUserNameChanged = {
            viewModel.onEvent(AccountScreenEvent.ChangeName(it))
        },
        availableColors = uiState.value.availableColors.map { it.toComposeColor() }.toSet(),
        selectedColors = uiState.value.selectedColors.map { it.toComposeColor() }.toSet(),
        onSelectedColorsChanged = {
            viewModel.onEvent(AccountScreenEvent.ChangeSelectedColors(it.map { c -> c.toFigureColor() }.toSet()))
        }
    )
}

@Composable
private fun AccountScreenContent(
    modifier: Modifier = Modifier,
    user: User,
    onUserNameChanged: (userName: String) -> Unit,
    availableColors: Set<Color>,
    selectedColors: Set<Color>,
    onSelectedColorsChanged: (Set<Color>) -> Unit
) {
    val contentDesc = stringResource(Res.string.feature_account_screen_content_desc)

    Column(
        modifier = modifier
            .then(modifier)
            .fillMaxSize()
            .padding(20.dp)
            .semantics {
                contentDescription = contentDesc
            },
        verticalArrangement = Arrangement.Center
    ) {
        UserInfoPanel(
            modifier = Modifier.fillMaxWidth(),
            user = user,
            onUserNameChanged = onUserNameChanged
        )

        StyleField(modifier = Modifier.padding(5.dp))
        ColorsField(
            modifier = Modifier.padding(5.dp),
            availableColors = availableColors,
            selectedColors = selectedColors,
            onSelectedColorsChanged = onSelectedColorsChanged
        )
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
                text = stringResource(Res.string.feature_account_selected_colors),
                modifier = Modifier.padding(5.dp)
            )
            Row(modifier = Modifier.padding(5.dp)) {
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
private fun StyleField(modifier: Modifier = Modifier) {
    PixelBorderBox(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = stringResource(Res.string.feature_account_selected_style),
                modifier = Modifier.padding(5.dp)
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
                                Figure.FigureType.TRIANGLE -> drawTriangleFigure(sizePx, Color.Green)
                            }
                        }
                    }
                }
            }
        }
    }
}
