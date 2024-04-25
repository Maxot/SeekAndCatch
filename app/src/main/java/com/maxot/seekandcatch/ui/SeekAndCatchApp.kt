package com.maxot.seekandcatch.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.maxot.seekandcatch.navigation.SeekCatchNavHost
import com.maxot.seekandcatch.navigation.TopLevelDestination

@Composable
fun SeekAndCatchApp(
    appState: SeekAndCatchAppState
) {
    Scaffold(
        bottomBar = {
            if (appState.shouldShowBottomBar) {
                BottomNavigationBar(
                    destinations = TopLevelDestination.entries,
                    onNavigateToDestination = appState::navigateToTopLevelDestination,
                    currentDestination = appState.currentDestination
                )
            }
        }
    ) { padding ->
        SeekCatchNavHost(
            appState = appState,
            modifier = Modifier.padding(padding)
        )
    }
}

fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
    this?.hierarchy?.any {
        it.route?.contains(destination.name, true) ?: false
    } ?: false
