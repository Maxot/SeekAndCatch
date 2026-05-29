package com.maxot.seekandcatch.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PixelProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    fillColor: Color = MaterialTheme.colorScheme.primary,
) {
    Canvas(modifier = modifier) {
        drawPixelBorders(
            drawScope = this,
            outerBorderColor = Color(0xFF1B3B24),
            middleBorderColor = Color(0xFFD6D68D),
            innerBorderColor = Color(0xFF1B3B24),
            backgroundColor = trackColor,
        )

        val bgStart = (4.dp + 6.dp + 4.dp).toPx()
        val innerWidth = size.width - 2 * bgStart
        val innerHeight = size.height - 2 * bgStart
        val fillWidth = (progress.coerceIn(0f, 1f) * innerWidth).toInt().toFloat()

        if (fillWidth > 0f) {
            drawRect(
                color = fillColor,
                topLeft = Offset(bgStart, bgStart),
                size = Size(fillWidth, innerHeight),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1A13)
@Composable
private fun PixelProgressBarPreview() {
    PixelProgressBar(
        progress = 0.65f,
        modifier = Modifier
            .width(280.dp)
            .height(20.dp),
        trackColor = Color(0xFF224F31),
        fillColor = Color(0xFFD6D68D),
    )
}
