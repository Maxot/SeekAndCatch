package com.maxot.seekandcatch.core.designsystem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

@Composable
fun PixelBorderBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val px = 6.dp.toPx()
                val w = size.width
                val h = size.height

                val borderColor = Color(0xFFD6D68D)

                val path = Path().apply {
                    moveTo(0f, px)
                    lineTo(0f, h - px)
                    lineTo(px, h)
                    lineTo(w - px, h)
                    lineTo(w, h - px)
                    lineTo(w, px)
                    lineTo(w - px, 0f)
                    lineTo(px, 0f)
                    close()
                }

                drawPath(path, color = borderColor)

                drawRect(
                    color = Color(0xFF254D30),
                    topLeft = Offset(px, px),
                    size = Size(w - 2 * px, h - 2 * px)
                )
            }
            .padding(16.dp)
            .background(Color(0xFF254D30)) // fallback background
    ) {
        content()
    }
}
