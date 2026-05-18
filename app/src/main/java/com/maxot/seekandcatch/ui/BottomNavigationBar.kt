package com.maxot.seekandcatch.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import com.maxot.seekandcatch.feature.settings.AudioController
import com.maxot.seekandcatch.navigation.TopLevelDestination
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun BottomNavigationBar(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
    val audioController = koinInject<AudioController>()
    NavigationBar(
        modifier = Modifier
            .then(modifier),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        destinations.forEach { destination ->
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
            val icon = if (selected) destination.selectedIcon else destination.unselectedIcon

            NavigationBarItem(
                label = {
                    Text(
                        text = stringResource(destination.iconTextId),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                    )
                },
                selected = selected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.secondary,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                    unselectedTextColor = MaterialTheme.colorScheme.secondary,
                    indicatorColor = MaterialTheme.colorScheme.onPrimary,
                ),
                onClick = {
                    audioController.onButtonClick()
                    onNavigateToDestination(destination)
                },
                icon = {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                    )
                },
            )
        }
    }
}
