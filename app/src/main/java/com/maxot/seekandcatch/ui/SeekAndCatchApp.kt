package com.maxot.seekandcatch.ui

import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.maxot.seekandcatch.core.designsystem.icon.SaCIcons
import com.maxot.seekandcatch.feature.settings.ui.SettingsDialog
import com.maxot.seekandcatch.navigation.SeekCatchNavHost
import com.maxot.seekandcatch.navigation.TopLevelDestination
import com.maxot.seekandcatch.feature.settings.R as SettingsR

@Composable
fun SeekAndCatchApp(
    appState: SeekAndCatchAppState
) {
    var showSettingsDialog by rememberSaveable {
        mutableStateOf(false)
    }
    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false }
        )
    }

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
                    onActionClick = { showSettingsDialog = true })
            }
        }
    ) { padding ->
        SeekCatchNavHost(
            appState = appState,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaCTopBar(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int,
    onActionClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        modifier = Modifier.then(modifier),
        title = {
            Text(
                text = stringResource(id = titleRes)
            )
        },
        actions = {
            IconButton(
                onClick = onActionClick
            ) {
                Icon(
                    imageVector = SaCIcons.Settings,
                    contentDescription = stringResource(id = SettingsR.string.feature_settings_top_app_bar_action_icon_content_desc)
                )
            }
        })
}

fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
    this?.hierarchy?.any {
        it.route?.contains(destination.name, true) ?: false
    } ?: false
