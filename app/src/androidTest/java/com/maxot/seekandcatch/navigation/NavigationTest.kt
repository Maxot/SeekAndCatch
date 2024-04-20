package com.maxot.seekandcatch.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.maxot.seekandcatch.MainActivity
import com.maxot.seekandcatch.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.maxot.seekandcatch.feature.gameplay.R as GameplayR

class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var mainScreenContentDesc: String
    private lateinit var flowGameScreenContentDesc: String
    private lateinit var startButtonText: String

    @Before
    fun setUp() {
        composeTestRule.activity.apply {
            mainScreenContentDesc = getString(R.string.main_screen_content_desc)
            flowGameScreenContentDesc = getString(GameplayR.string.flow_game_screen_content_desc)
            startButtonText = getString(R.string.button_start_game_text)
        }
    }

    @Test
    fun appCreated_mainScreenDisplayed() {
        composeTestRule
            .onNodeWithContentDescription(mainScreenContentDesc)
            .assertIsDisplayed()
    }

    @Test
    fun mainScreen__clickStartGame_navigateToFlowGameScreen() {
        composeTestRule.onNodeWithText(startButtonText)
            .performClick()

        composeTestRule.onNodeWithContentDescription(flowGameScreenContentDesc)
            .assertIsDisplayed()
    }
}
