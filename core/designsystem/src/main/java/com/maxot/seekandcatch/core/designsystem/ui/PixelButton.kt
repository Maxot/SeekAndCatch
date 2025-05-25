package com.maxot.seekandcatch.core.designsystem.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.core.designsystem.theme.pixelFont

@Composable
fun PixelButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    buttonColor: Color = MaterialTheme.colorScheme.primaryContainer,
    borderColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    content: @Composable RowScope.() -> Unit
) {
    val px = with(LocalDensity.current) { 2.dp.toPx() }

    Box(
        modifier = modifier
            .wrapContentSize()
            .clickable { onClick() }
            .drawBehind {
                val w = size.width
                val h = size.height
                val borderSize = px * 2

                // Border path
                val borderPath = Path().apply {
                    moveTo(0f, borderSize)
                    lineTo(0f, h - borderSize)
                    lineTo(borderSize, h)
                    lineTo(w - borderSize, h)
                    lineTo(w, h - borderSize)
                    lineTo(w, borderSize)
                    lineTo(w - borderSize, 0f)
                    lineTo(borderSize, 0f)
                    close()
                }
                drawPath(borderPath, color = borderColor)

                // Button background path
                val inset = borderSize
                val path = Path().apply {
                    moveTo(inset, inset + px)
                    lineTo(inset, h - inset - px)
                    lineTo(inset + px, h - inset)
                    lineTo(w - inset - px, h - inset)
                    lineTo(w - inset, h - inset - px)
                    lineTo(w - inset, inset + px)
                    lineTo(w - inset - px, inset)
                    lineTo(inset + px, inset)
                    close()
                }
                drawPath(path, color = buttonColor)
            }
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Preview
@Composable
private fun PixelButtonPreview() {
    SeekAndCatchTheme {
        PixelButton(
            onClick = {}
        ) {
            Text(
                text = "Hello World",
                fontFamily = pixelFont
            )
        }
    }
}
