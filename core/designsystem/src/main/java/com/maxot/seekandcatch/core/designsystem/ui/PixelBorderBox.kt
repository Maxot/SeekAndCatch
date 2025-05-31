package com.maxot.seekandcatch.core.designsystem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun PixelBorderBox(
    modifier: Modifier = Modifier,
    outerBorderColor: Color = Color(0xFF1B3B24),
    middleBorderColor: Color = Color(0xFFD6D68D),
    innerBorderColor: Color = Color(0xFF1B3B24),
    backgroundColor: Color = Color(0xFF254D30),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawPixelBorders(
                    this,
                    outerBorderColor,
                    middleBorderColor,
                    innerBorderColor,
                    backgroundColor
                )
            }
            .padding(16.dp)
            .background(backgroundColor)
    ) {
        content()
    }
}


@Preview
@Composable
private fun PixelBorderBoxPreview() {
    PixelBorderBox(
        modifier = Modifier.size(200.dp)
    ) {
        Text(text = "Preview", color = Color.White)
    }
}

fun drawPixelBorders(
    drawScope: DrawScope,
    outerBorderColor: Color = Color(0xFF1B3B24),
    middleBorderColor: Color = Color(0xFFD6D68D),
    innerBorderColor: Color = Color(0xFF1B3B24),
    backgroundColor: Color = Color(0xFF254D30),
) = with(drawScope) {
    val outerBorderWidth = 4.dp.toPx()
    val middleBorderWidth = 6.dp.toPx()
    val innerBorderWidth = 4.dp.toPx()

    val w = size.width
    val h = size.height

    val middleStart = outerBorderWidth
    val innerStart = middleStart + middleBorderWidth
    val bgStart = innerStart + innerBorderWidth

    val outerBorderPath = Path().apply {
        // left
        moveTo(0f, outerBorderWidth * 2)
        lineTo(0f, h - outerBorderWidth * 2)
        // bottom-left corner
        lineTo(outerBorderWidth * 2, h)
        lineTo(w - outerBorderWidth * 2, h)
        // bottom-right corner
        lineTo(w, h - outerBorderWidth * 2)
        lineTo(w, outerBorderWidth * 2)
        // top-right corner
        lineTo(w - outerBorderWidth * 2, 0f)
        lineTo(outerBorderWidth * 2, 0f)
        // top-left corner
        close()
    }

    val middleBorderPath = Path().apply {
        moveTo(middleStart, middleStart + middleBorderWidth)
        lineTo(middleStart, h - middleStart - middleBorderWidth)
        lineTo(middleStart + middleBorderWidth, h - middleStart)
        lineTo(w - middleStart - middleBorderWidth, h - middleStart)
        lineTo(w - middleStart, h - middleStart - middleBorderWidth)
        lineTo(w - middleStart, middleStart + middleBorderWidth)
        lineTo(w - middleStart - middleBorderWidth, middleStart)
        lineTo(middleStart + middleBorderWidth, middleStart)
        close()
    }

    val innerBorderPath = Path().apply {
        moveTo(innerStart, innerStart + innerBorderWidth)
        lineTo(innerStart, h - innerStart - innerBorderWidth)
        lineTo(innerStart + innerBorderWidth, h - innerStart)
        lineTo(w - innerStart - innerBorderWidth, h - innerStart)
        lineTo(w - innerStart, h - innerStart - innerBorderWidth)
        lineTo(w - innerStart, innerStart + innerBorderWidth)
        lineTo(w - innerStart - innerBorderWidth, innerStart)
        lineTo(innerStart + innerBorderWidth, innerStart)
        close()
    }

    drawPath(path = outerBorderPath, color = outerBorderColor)
    drawPath(path = middleBorderPath, color = middleBorderColor)
    drawPath(path = innerBorderPath, color = innerBorderColor)

    drawRect(
        color = backgroundColor,
        topLeft = Offset(bgStart, bgStart),
        size = Size(w - 2 * bgStart, h - 2 * bgStart)
    )
}
