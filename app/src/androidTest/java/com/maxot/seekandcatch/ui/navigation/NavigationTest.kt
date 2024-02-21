package com.maxot.seekandcatch.ui.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.maxot.seekandcatch.ui.SeekAndCatchAppState
import kotlinx.coroutines.test.TestScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    lateinit var navController: TestNavHostController

    @Before
    fun setupAppNavHost() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            val appState = SeekAndCatchAppState(navController = navController, coroutineScope = TestScope().backgroundScope)

            SeekCatchNavHost(appState)
        }
    }

    @Test
    fun seekCatchNavHost_verifyStartDestination() {
        composeTestRule
            .onNodeWithContentDescription("Main Screen")
            .assertIsDisplayed()
//        assertEquals(1, 1)
    }

//    @Test
//    fun seekCatchNavHost_clickFlowGame_navigateToFlowGame() {
//        composeTestRule.onNodeWithContentDescription("All Profiles")
//            .performClick()
//
//        val route = navController.currentBackStackEntry?.destination?.route
//        assertEquals(route, Screen.FlowGameScreen.route)
//    }
}