package com.maxot.seekandcatch.core.designsystem.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.abs

fun DrawScope.drawSquareFigure(size: Float, baseColor: Color) {
    val pixelSize = size / 10f

    fun saturate(color: Color, factor: Float): Color {
        val r = color.red
        val g = color.green
        val b = color.blue
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val l = (max + min) / 2f
        val d = max - min
        val s = if (d == 0f) 0f else d / (1f - abs(2 * l - 1f))
        val newS = (s * factor).coerceIn(0f, 1f)
        val h = when {
            d == 0f -> 0f
            max == r -> 60f * (((g - b) / d) % 6f)
            max == g -> 60f * (((b - r) / d) + 2f)
            else -> 60f * (((r - g) / d) + 4f)
        }
        val c = (1f - abs(2 * l - 1f)) * newS
        val x = c * (1f - abs((h / 60f) % 2 - 1f))
        val m = l - c / 2f
        val (r1, g1, b1) = when {
            h < 0f -> Triple(0f, 0f, 0f)
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            h <= 360f -> Triple(c, 0f, x)
            else -> Triple(0f, 0f, 0f)
        }
        return Color(
            (r1 + m).coerceIn(0f, 1f),
            (g1 + m).coerceIn(0f, 1f),
            (b1 + m).coerceIn(0f, 1f),
            color.alpha
        )
    }

    val highlightColor = Color.White
    val shadowColor = Color(0xFF191919)
    val darkShadowColor = Color(0xFF000000)
    val lightHighlight =
        saturate(baseColor, 1.7f).copy(alpha = 1f).compositeOver(Color.White.copy(alpha = 0.25f))
    val darkShadow =
        saturate(baseColor, 0.3f).copy(alpha = 1f).compositeOver(Color.Black.copy(alpha = 0.22f))

    fun pixel(x: Int, y: Int, color: Color) {
        drawRect(
            color = color,
            topLeft = Offset(x * pixelSize, y * pixelSize),
            size = Size(pixelSize, pixelSize)
        )
    }

    val used = mutableSetOf<Pair<Int, Int>>()
    for (x in 1..8) {
        for (y in 1..8) {
            used += x to y
            pixel(x, y, baseColor)
        }
    }
    // highlight: top edge and left edge
    for (x in 1..8) {
        pixel(x, 1, lightHighlight)
        used += x to 1
    }
    for (y in 1..8) {
        pixel(1, y, lightHighlight)
        used += 1 to y
    }
    // shadow: right edge and bottom edge
    for (y in 1..8) {
        pixel(8, y, darkShadow)
        used += 8 to y
    }
    for (x in 1..8) {
        pixel(x, 8, darkShadow)
        used += x to 8
    }
    // extra highlight at (2,2) for a retro pixel shine
    pixel(2, 2, highlightColor)
    used += 2 to 2
    // extra shadow at (7,7) for retro pixel shadow
    pixel(7, 7, darkShadowColor)
    used += 7 to 7

    // Draw black border around used pixels
    val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
    for ((x, y) in used) {
        for ((dx, dy) in directions) {
            val nx = x + dx
            val ny = y + dy
            if (nx to ny !in used) {
                pixel(nx, ny, Color.Black)
            }
        }
    }
}

fun DrawScope.drawCircleFigure(size: Float, baseColor: Color) {
    val pixelSize = size / 10f

    fun saturate(color: Color, factor: Float): Color {
        val r = color.red
        val g = color.green
        val b = color.blue
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val l = (max + min) / 2f
        val d = max - min
        val s = if (d == 0f) 0f else d / (1f - abs(2 * l - 1f))
        val newS = (s * factor).coerceIn(0f, 1f)
        val h = when {
            d == 0f -> 0f
            max == r -> 60f * (((g - b) / d) % 6f)
            max == g -> 60f * (((b - r) / d) + 2f)
            else -> 60f * (((r - g) / d) + 4f)
        }
        val c = (1f - abs(2 * l - 1f)) * newS
        val x = c * (1f - abs((h / 60f) % 2 - 1f))
        val m = l - c / 2f
        val (r1, g1, b1) = when {
            h < 0f -> Triple(0f, 0f, 0f)
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            h <= 360f -> Triple(c, 0f, x)
            else -> Triple(0f, 0f, 0f)
        }
        return Color(
            (r1 + m).coerceIn(0f, 1f),
            (g1 + m).coerceIn(0f, 1f),
            (b1 + m).coerceIn(0f, 1f),
            color.alpha
        )
    }

    val highlightColor = Color.White
    val shadowColor = Color(0xFF191919)
    val darkShadowColor = Color(0xFF000000)
    val lightHighlight =
        saturate(baseColor, 1.7f).copy(alpha = 1f).compositeOver(Color.White.copy(alpha = 0.25f))
    val darkShadow =
        saturate(baseColor, 0.3f).copy(alpha = 1f).compositeOver(Color.Black.copy(alpha = 0.22f))

    fun pixel(x: Int, y: Int, color: Color) {
        drawRect(
            color = color,
            topLeft = Offset(x * pixelSize, y * pixelSize),
            size = Size(pixelSize, pixelSize)
        )
    }

    val used = mutableSetOf<Pair<Int, Int>>()
    val main = listOf(
        3 to 1, 4 to 1, 5 to 1, 6 to 1,
        2 to 2, 3 to 2, 4 to 2, 5 to 2, 6 to 2, 7 to 2,
        1 to 3, 2 to 3, 3 to 3, 4 to 3, 5 to 3, 6 to 3, 7 to 3, 8 to 3,
        1 to 4, 2 to 4, 3 to 4, 4 to 4, 5 to 4, 6 to 4, 7 to 4, 8 to 4,
        1 to 5, 2 to 5, 3 to 5, 4 to 5, 5 to 5, 6 to 5, 7 to 5, 8 to 5,
        1 to 6, 2 to 6, 3 to 6, 4 to 6, 5 to 6, 6 to 6, 7 to 6, 8 to 6,
        2 to 7, 3 to 7, 4 to 7, 5 to 7, 6 to 7, 7 to 7,
        3 to 8, 4 to 8, 5 to 8, 6 to 8
    )
    main.forEach { (x, y) ->
        pixel(x, y, baseColor)
        used += x to y
    }

    val highlights = listOf(
        3 to 1, 4 to 1, 2 to 2, 1 to 3, 1 to 4, 1 to 5
    )
    highlights.forEach {
        pixel(it.first, it.second, lightHighlight)
        used += it
    }

    pixel(4, 2, highlightColor)
    used += 4 to 2

    val shadows = listOf(
        6 to 8, 7 to 7, 8 to 6, 8 to 5,
    )
    shadows.forEach {
        pixel(it.first, it.second, darkShadow)
        used += it
    }

    used += 6 to 8

    // Draw black border around used pixels
    val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
    for ((x, y) in used) {
        for ((dx, dy) in directions) {
            val nx = x + dx
            val ny = y + dy
            if (nx to ny !in used) {
                pixel(nx, ny, Color.Black)
            }
        }
    }
}

fun DrawScope.drawTriangleFigure(size: Float, baseColor: Color) {
    val pixelSize = size / 10f

    fun saturate(color: Color, factor: Float): Color {
        val r = color.red
        val g = color.green
        val b = color.blue
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val l = (max + min) / 2f
        val d = max - min
        val s = if (d == 0f) 0f else d / (1f - abs(2 * l - 1f))
        val newS = (s * factor).coerceIn(0f, 1f)
        val h = when {
            d == 0f -> 0f
            max == r -> 60f * (((g - b) / d) % 6f)
            max == g -> 60f * (((b - r) / d) + 2f)
            else -> 60f * (((r - g) / d) + 4f)
        }
        val c = (1f - abs(2 * l - 1f)) * newS
        val x = c * (1f - abs((h / 60f) % 2 - 1f))
        val m = l - c / 2f
        val (r1, g1, b1) = when {
            h < 0f -> Triple(0f, 0f, 0f)
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            h <= 360f -> Triple(c, 0f, x)
            else -> Triple(0f, 0f, 0f)
        }
        return Color(
            (r1 + m).coerceIn(0f, 1f),
            (g1 + m).coerceIn(0f, 1f),
            (b1 + m).coerceIn(0f, 1f),
            color.alpha
        )
    }

    val highlightColor = Color.White
    val shadowColor = Color(0xFF191919)
    val darkShadowColor = Color(0xFF000000)
    val lightHighlight =
        saturate(baseColor, 1.7f).copy(alpha = 1f).compositeOver(Color.White.copy(alpha = 0.25f))
    val darkShadow =
        saturate(baseColor, 0.3f).copy(alpha = 1f).compositeOver(Color.Black.copy(alpha = 0.22f))

    fun pixel(x: Int, y: Int, color: Color) {
        drawRect(
            color = color,
            topLeft = Offset(x * pixelSize, y * pixelSize),
            size = Size(pixelSize, pixelSize)
        )
    }

    val used = mutableSetOf<Pair<Int, Int>>()
    val tri = listOf(
        5 to 2,
        4 to 3, 5 to 3, 6 to 3,
        4 to 4, 5 to 4, 6 to 4,
        3 to 5, 4 to 5, 5 to 5, 6 to 5, 7 to 5,
        2 to 6, 3 to 6, 4 to 6, 5 to 6, 6 to 6, 7 to 6,
        2 to 7, 3 to 7, 4 to 7, 5 to 7, 6 to 7, 7 to 7,
        1 to 8, 2 to 8, 3 to 8, 4 to 8, 5 to 8, 6 to 8, 7 to 8, 8 to 8, 7 to 6
    )
    tri.forEach { (x, y) ->
        pixel(x, y, baseColor)
        used += x to y
    }
    // highlight: left/top edge of the triangle
    val highlights = listOf(
        5 to 2,
        4 to 3,
        4 to 4,
        3 to 5,
        2 to 6,
        2 to 7,
        1 to 8, 2 to 8, 3 to 8, 4 to 8
    )
    highlights.forEach { (x, y) -> pixel(x, y, lightHighlight) }
    // pixel shine at the triangle peak
    pixel(5, 2, highlightColor)
    // shadow: right/bottom edge of the triangle
    val shadows = listOf(
        6 to 3,
        6 to 4,
        7 to 5,
        7 to 6,
        8 to 6,
        7 to 7,
        8 to 8
    )
    shadows.forEach { (x, y) -> pixel(x, y, darkShadow) }
    // extra dark shadow for retro
//    pixel(8, 8, darkShadowColor)

    // Draw black border around used pixels
    val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
    for ((x, y) in used) {
        for ((dx, dy) in directions) {
            val nx = x + dx
            val ny = y + dy
            if (nx to ny !in used) {
                pixel(nx, ny, Color.Black)
            }
        }
    }
    pixel(9, 9, Color.Black)
    pixel(0, 9, Color.Black)
}