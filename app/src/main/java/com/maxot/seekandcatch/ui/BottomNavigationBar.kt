package com.maxot.seekandcatch.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination
import com.maxot.seekandcatch.core.designsystem.theme.SeekAndCatchTheme
import com.maxot.seekandcatch.navigation.TopLevelDestination

@Composable
fun BottomNavigationBar(
    destinations: List<TopLevelDestination>,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier,
) {
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
                    Text(stringResource(id = destination.iconTextId))
                },
                selected = selected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.secondary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.secondary,
                    indicatorColor = MaterialTheme.colorScheme.onPrimary,
                ),
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    SeekAndCatchTheme {
        BottomNavigationBar(
            destinations = TopLevelDestination.entries,
            onNavigateToDestination = {},
            currentDestination = null
        )
    }
}
