package com.maxot.seekandcatch.feature.gameplay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.random.Random

fun Int.Companion.getRandomNumber(): Int {
    return Random.nextInt(4)
}

fun Float.getDecimalPart(): Float {
    val integerPart = this.toInt()
    return this - integerPart
}

fun Modifier.shake(
    enabled: Boolean,
    duration: Int = 500,
    strength: Float = 10f
): Modifier = composed {
    val shakeOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    LaunchedEffect(enabled) {
        if (enabled) {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < duration) {
                shakeOffset.animateTo(
                    targetValue = Offset(
                        Random.nextFloat() * strength * 2 - strength,
                        Random.nextFloat() * strength * 2 - strength
                    ),
                    animationSpec = tween(durationMillis = 50)
                )
            }
            shakeOffset.animateTo(Offset.Zero)
        } else {
            shakeOffset.snapTo(Offset.Zero)
        }
    }

    this.graphicsLayer {
        translationX = shakeOffset.value.x
        translationY = shakeOffset.value.y
    }
}

fun Modifier.flashRed(
    enabled: Boolean
): Modifier = composed {
    val color = animateColorAsState(
        targetValue = if (enabled) Color.Red.copy(alpha = 0.5f) else Color.Transparent,
        animationSpec = tween(durationMillis = 250),
        label = "FlashRed"
    )

    this.graphicsLayer {
        clip = true
    }.background(color.value)
}

val Offset.Companion.VectorConverter: TwoWayConverter<Offset, AnimationVector2D>
    get() = TwoWayConverter(
        convertToVector = { AnimationVector2D(it.x, it.y) },
        convertFromVector = { Offset(it.v1, it.v2) }
    )
