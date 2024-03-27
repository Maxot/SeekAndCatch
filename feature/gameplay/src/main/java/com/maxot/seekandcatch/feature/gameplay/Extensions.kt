package com.maxot.seekandcatch.feature.gameplay

import androidx.compose.ui.graphics.Color
import kotlin.random.Random


fun Color.Companion.getRandomColor(seed: Int = Int.getRandomNumber()): Color {
    return when (seed) {
        0 -> Red
        1 -> Blue
        2 -> Green
        3 -> Yellow
        else -> Red
    }
}

fun Int.Companion.getRandomNumber(): Int {
    return Random.nextInt(4)
}

fun Float.removeIntegerPart(): Float {
    val integerPart = this.toInt()
    return this - integerPart
}
