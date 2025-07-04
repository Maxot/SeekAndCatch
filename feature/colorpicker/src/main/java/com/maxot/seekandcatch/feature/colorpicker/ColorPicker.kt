package com.maxot.seekandcatch.feature.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.designsystem.theme.Shapes
import com.maxot.seekandcatch.core.designsystem.ui.PixelBorderBox
import com.maxot.seekandcatch.core.designsystem.ui.PixelButton

@Composable
fun ColorPicker(
    modifier: Modifier = Modifier,
    availableColors: Set<Color>,
    selectedColor: Color,
    onSelectedColorChanged: (Color) -> Unit
) {
    var showColorSelectionDialog by remember {
        mutableStateOf(false)
    }
    var currentlySelectedColor by remember {
        mutableStateOf(selectedColor)
    }
    Box(
        modifier = Modifier
            .then(modifier)
            .clip(Shapes.large)
            .background(color = currentlySelectedColor)
            .size(50.dp)
            .clickable { showColorSelectionDialog = true })

    if (showColorSelectionDialog) {
        ColorSelectionDialog(
            availableColors = availableColors,
            selectedColor = currentlySelectedColor,
            onConfirmation = { color ->
                currentlySelectedColor = color
                onSelectedColorChanged(currentlySelectedColor)
                showColorSelectionDialog = false
            },
            onDismissRequest = {
                showColorSelectionDialog = false
            }
        )
    }
}

@Composable
private fun ColorSelectionDialog(
    modifier: Modifier = Modifier,
    availableColors: Set<Color>,
    selectedColor: Color,
    onDismissRequest: () -> Unit,
    onConfirmation: (Color) -> Unit,
) {
    var currentlySelectedColor by remember {
        mutableStateOf(selectedColor)
    }
    AlertDialog(
        modifier = modifier,
        title = null,
        containerColor = Color.Transparent,
        text = {
            PixelBorderBox {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.feature_color_picker_title)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(50.dp),
                        verticalArrangement = Arrangement.SpaceAround,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        items(availableColors.toList()) { color ->
                            ColorLayout(
                                isSelected = color == currentlySelectedColor,
                                color = color,
                                onColorSelected = { newColor ->
                                    currentlySelectedColor = newColor
                                })
                        }
                    }

                    PixelButton(
                        modifier = Modifier,
                        paddingValues = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        onClick = { onConfirmation(currentlySelectedColor) })
                    {
                        Text(
                            modifier = Modifier.padding(20.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            text = stringResource(id = R.string.feature_color_picker_dialog_button_ok)
                        )
                    }
                }
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {}
    )
}

@Composable
fun ColorLayout(
    isSelected: Boolean,
    color: Color,
    onColorSelected: (Color) -> Unit
) {
    val shape = RoundedCornerShape(corner = CornerSize(10.dp))
    Box(
        modifier = Modifier
            .padding(5.dp)
            .size(50.dp)
            .clip(shape)
            .background(color)
            .border(
                width = 4.dp,
                color = if (isSelected) Color.Black else Color.White,
                shape = shape
            )
            .clickable {
                onColorSelected(color)
            }

    )
}

@Preview
@Composable
private fun ColorPickerPreview() {
    SeekAndCatchTheme {
        ColorPicker(
            availableColors = setOf(
                Color.Red,
                Color.Blue,
                Color.Green,
                Color.White
            ),
            selectedColor = Color.Red,
            onSelectedColorChanged = { }
        )
    }
}

@Preview
@Composable
private fun ColorSelectionDialogPreview() {
    SeekAndCatchTheme {
        ColorSelectionDialog(
            availableColors = setOf(
                Color.Red,
                Color.Blue,
                Color.Green,
                Color.White,
            ),
            selectedColor = Color.Red,
            onDismissRequest = {},
            onConfirmation = { }
        )
    }
}
