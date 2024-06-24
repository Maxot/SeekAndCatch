package com.maxot.seekandcatch.feature.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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
    Box(modifier = Modifier
        .then(modifier)
        .clip(RoundedCornerShape(corner = CornerSize(10.dp)))
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
fun ColorSelectionDialog(
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
        modifier = Modifier
            .then(modifier),
        title = {
            Text(text = stringResource(id = R.string.feature_color_picker_title))
        },
        text = {
            LazyVerticalGrid(
                modifier = Modifier,
                columns = GridCells.Adaptive(50.dp)
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
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onConfirmation(currentlySelectedColor) }) {
                Text(text = stringResource(id = R.string.feature_color_picker_dialog_button_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text(text = stringResource(id = R.string.feature_color_picker_dialog_button_cancel))
            }
        })
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
            .size(50.dp)
            .clip(shape)
            .background(color)
            .border(
                width = 2.dp,
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
fun ColorPickerPreview() {
    ColorPicker(
        availableColors = setOf(
            Color.Red,
            Color.Blue,
            Color.Green
        ),
        selectedColor = Color.Red,
        onSelectedColorChanged = { }
    )
}

@Preview
@Composable
fun ColorSelectionDialogPreview() {
    ColorSelectionDialog(
        availableColors = setOf(
            Color.Red,
            Color.Blue,
            Color.Green
        ),
        selectedColor = Color.Red,
        onDismissRequest = {},
        onConfirmation = { }
    )
}
