package com.maxot.seekandcatch.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PixelToggle(
    isOn: Boolean,
    onToggle: () -> Unit
) {
    val toggleWidth = 56.dp
    val toggleHeight = 28.dp
    val borderColor = Color(0xFFD6D68D)
    val fillColor = Color(0xFF2E5B3D)
    val knobColor = Color(0xFFD6D68D)
    val shadowColor = Color.Black
    val step = 4.dp

    Box(
        modifier = Modifier
            .size(toggleWidth, toggleHeight)
            .drawBehind {
                val px = step.toPx()
                val w = size.width
                val h = size.height

                val path = Path().apply {
                    moveTo(px * 2, 0f)
                    lineTo(w - px * 2, 0f)
                    lineTo(w, px * 2)
                    lineTo(w, h - px * 2)
                    lineTo(w - px * 2, h)
                    lineTo(px * 2, h)
                    lineTo(0f, h - px * 2)
                    lineTo(0f, px * 2)
                    close()
                }

                // Outer rounded pixel border
                drawPath(path, borderColor)

                // Inner fill
                drawRect(
                    color = fillColor,
                    topLeft = Offset(px, px),
                    size = Size(w - 2 * px, h - 2 * px)
                )
            }
            .clickable { onToggle() },
        contentAlignment = if (isOn) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(14.dp)
                .drawBehind {
                    drawRect(shadowColor)
                    drawRect(
                        color = knobColor,
                        topLeft = Offset(1f, 1f),
                        size = size.copy(width = size.width - 2f, height = size.height - 2f)
                    )
                }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PixelTogglePreview() {
    var isOn by remember { mutableStateOf(true) }
    PixelToggle(
        isOn = isOn,
        onToggle = { isOn = !isOn }
    )
}