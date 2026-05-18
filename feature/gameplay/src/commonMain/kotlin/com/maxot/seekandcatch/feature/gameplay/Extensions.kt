package com.maxot.seekandcatch.feature.gameplay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.toSize
import kotlin.random.Random
import kotlinx.coroutines.withTimeout

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
            withTimeout(duration.toLong()) {
                while (true) {
                    shakeOffset.animateTo(
                        targetValue = Offset(
                            Random.nextFloat() * strength * 2 - strength,
                            Random.nextFloat() * strength * 2 - strength
                        ),
                        animationSpec = tween(durationMillis = 50)
                    )
                }
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

fun Modifier.moveAndScale(
    targetCoordinates: LayoutCoordinates?,
    isAtTarget: Boolean,
    animationDuration: Int = 1000,
    easing: Easing = LinearEasing
): Modifier = composed {
    var currentCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val translationX by animateFloatAsState(
        targetValue = if (isAtTarget && targetCoordinates != null && currentCoordinates != null) {
            targetCoordinates.positionInRoot().x - currentCoordinates!!.positionInRoot().x
        } else 0f,
        animationSpec = tween(durationMillis = animationDuration, easing = easing),
        label = "translationX"
    )

    val translationY by animateFloatAsState(
        targetValue = if (isAtTarget && targetCoordinates != null && currentCoordinates != null) {
            targetCoordinates.positionInRoot().y - currentCoordinates!!.positionInRoot().y
        } else 0f,
        animationSpec = tween(durationMillis = animationDuration, easing = easing),
        label = "translationY"
    )

    val scale by animateFloatAsState(
        targetValue = if (isAtTarget && targetCoordinates != null && currentCoordinates != null && currentCoordinates!!.size.toSize().width > 0) {
            targetCoordinates.size.toSize().width / currentCoordinates!!.size.toSize().width
        } else 1f,
        animationSpec = tween(durationMillis = animationDuration, easing = easing),
        label = "scale"
    )

    this
        .onGloballyPositioned { currentCoordinates = it }
        .graphicsLayer {
            this.translationX = translationX
            this.translationY = translationY
            this.scaleX = scale
            this.scaleY = scale
            this.transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
        }
}

val Offset.Companion.VectorConverter: TwoWayConverter<Offset, AnimationVector2D>
    get() = TwoWayConverter(
        convertToVector = { AnimationVector2D(it.x, it.y) },
        convertFromVector = { Offset(it.v1, it.v2) }
    )
