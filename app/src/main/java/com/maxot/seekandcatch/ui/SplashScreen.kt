package com.maxot.seekandcatch.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maxot.seekandcatch.R
import com.maxot.seekandcatch.core.designsystem.component.PixelProgressBar
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import kotlinx.coroutines.delay

private const val SPLASH_MIN_DURATION_MS = 1500L

@Composable
fun SplashScreen(
    isAppReady: () -> Boolean,
    onSplashComplete: () -> Unit,
) {
    var progressTarget by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = SPLASH_MIN_DURATION_MS.toInt(), easing = LinearEasing),
        label = "splashProgress",
    )

    LaunchedEffect(Unit) {
        progressTarget = 1f
        val startTime = System.currentTimeMillis()
        while (!isAppReady() || System.currentTimeMillis() - startTime < SPLASH_MIN_DURATION_MS) {
            delay(16)
        }
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_splash),
                contentDescription = null,
                modifier = Modifier.size(200.dp),
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Seek & Catch",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(48.dp))
            PixelProgressBar(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(20.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                fillColor = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    SeekAndCatchTheme {
        SplashScreen(
            isAppReady = { false },
            onSplashComplete = {},
        )
    }
}
