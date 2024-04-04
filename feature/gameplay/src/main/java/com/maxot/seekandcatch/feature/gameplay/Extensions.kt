package com.maxot.seekandcatch.feature.gameplay

import kotlin.random.Random


fun Int.Companion.getRandomNumber(): Int {
    return Random.nextInt(4)
}

fun Float.getDecimalPart(): Float {
    val integerPart = this.toInt()
    return this - integerPart
}
