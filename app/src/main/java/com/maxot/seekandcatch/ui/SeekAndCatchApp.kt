package com.maxot.seekandcatch.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.maxot.seekandcatch.core.common.VisualFeedbackManager
import com.maxot.seekandcatch.feature.settings.AudioController
import com.maxot.seekandcatch.feature.settings.ui.SettingsDialog
import com.maxot.seekandcatch.navigation.SeekCatchNavHost
import com.maxot.seekandcatch.navigation.TopLevelDestination
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import com.maxot.seekandcatch.core.designsystem.generated.resources.Res as DesignRes
import com.maxot.seekandcatch.core.designsystem.generated.resources.background
import com.maxot.seekandcatch.core.designsystem.generated.resources.ic_settings
import com.maxot.seekandcatch.feature.settings.generated.resources.Res as SettingsRes
import com.maxot.seekandcatch.feature.settings.generated.resources.feature_settings_top_app_bar_action_icon_content_desc

@Composable
fun SeekAndCatchApp(
    appState: SeekAndCatchAppState,
    visualFeedbackManager: VisualFeedbackManager
) {
    var showSettingsDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val audioController = koinInject<AudioController>()
    val isLifeWasted by visualFeedbackManager.isLifeWasted.collectAsStateWithLifecycle()

    val largeRadialGradient = object : ShaderBrush() {
        override fun createShader(size: Size): Shader {
            val biggerDimension = maxOf(size.height, size.width)
            return RadialGradientShader(
                colors = listOf(Color.Transparent, Color.Red.copy(alpha = 0.4f)),
                center = size.center,
                radius = biggerDimension / 2f,
                colorStops = listOf(0f, 0.95f)
            )
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (appState.shouldShowBottomBar) {
                    BottomNavigationBar(
                        destinations = TopLevelDestination.entries,
                        onNavigateToDestination = appState::navigateToTopLevelDestination,
                        currentDestination = appState.currentDestination
                    )
                }
            },
            topBar = {
                appState.currentTopLevelDestination?.let {
                    SaCTopBar(
                        titleRes = it.titleTextId,
                        onActionClick = {
                            audioController.onButtonClick()
                            showSettingsDialog = true
                        })
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(DesignRes.drawable.background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                SeekCatchNavHost(
                    appState = appState,
                    modifier = Modifier.padding(padding)
                )
            }
        }

        if (isLifeWasted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(largeRadialGradient)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaCTopBar(
    modifier: Modifier = Modifier,
    titleRes: StringResource,
    onActionClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        modifier = Modifier.then(modifier),
        title = {
            Text(
                text = stringResource(titleRes)
            )
        },
        actions = {
            IconButton(
                onClick = onActionClick
            ) {
                Icon(
                    painter = painterResource(DesignRes.drawable.ic_settings),
                    contentDescription = stringResource(SettingsRes.string.feature_settings_top_app_bar_action_icon_content_desc)
                )
            }
        })
}

fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
    this?.hierarchy?.any {
        it.route?.contains(destination.name, true) ?: false
    } ?: false
