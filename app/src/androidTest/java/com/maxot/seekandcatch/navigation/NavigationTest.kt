package com.maxot.seekandcatch.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.maxot.seekandcatch.MainActivity
import com.maxot.seekandcatch.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.maxot.seekandcatch.feature.account.R as AccountR
import com.maxot.seekandcatch.feature.gameplay.R as GameplayR
import com.maxot.seekandcatch.feature.leaderboard.R as LeaderboardR
import com.maxot.seekandcatch.feature.settings.R as SettingsR

class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var game: String
    private lateinit var leaderboard: String
    private lateinit var account: String
    private lateinit var settings: String


    private lateinit var settingsIconContentDesc: String
    private lateinit var gameSelectionScreenContentDesc: String
    private lateinit var leaderboardScreenContentDesc: String
    private lateinit var accountScreenContentDesc: String
    private lateinit var flowGameScreenContentDesc: String
    private lateinit var startButtonText: String

    @Before
    fun setUp() {
        composeTestRule.activity.apply {
            game = getString(GameplayR.string.feature_gameplay_title)
            leaderboard = getString(LeaderboardR.string.feature_leaderboard_title)
            account = getString(AccountR.string.feature_account_title)
            settings = getString(SettingsR.string.feature_settings_title)

            settingsIconContentDesc =
                getString(SettingsR.string.feature_settings_top_app_bar_action_icon_content_desc)
            accountScreenContentDesc =
                getString(AccountR.string.feature_account_screen_content_desc)
            leaderboardScreenContentDesc =
                getString(LeaderboardR.string.feature_leaderboard_screen_content_desc)
            gameSelectionScreenContentDesc =
                getString(GameplayR.string.feature_gameplay_game_selection_screen_content_desc)
            flowGameScreenContentDesc = getString(GameplayR.string.flow_game_screen_content_desc)
            startButtonText = getString(R.string.button_start_game_text)
        }
    }

    @Test
    fun firstScreen_isGameSelectionScreen() {
        composeTestRule
            .onNodeWithContentDescription(gameSelectionScreenContentDesc)
            .assertIsDisplayed()
    }

    @Test
    fun navigationBar_clickLeaderboard_leaderboardScreenShown() {
        composeTestRule
            .onNodeWithText(leaderboard)
            .performClick()

        composeTestRule
            .onNodeWithContentDescription(leaderboardScreenContentDesc)
            .isDisplayed()
    }

    @Test
    fun navigationBar_clickAccount_accountScreenShown() {
        composeTestRule
            .onNodeWithText(account)
            .performClick()

        composeTestRule
            .onNodeWithContentDescription(accountScreenContentDesc)
            .isDisplayed()
    }

    @Test
    fun gameScreen_clickStartGame_navigateToFlowGameScreen() {
        composeTestRule.onNodeWithText(startButtonText)
            .performClick()

        composeTestRule.onNodeWithContentDescription(flowGameScreenContentDesc)
            .assertIsDisplayed()
    }

    @Test
    fun topLevelDestinations_showSettingsIcon() {
        composeTestRule.apply {
            onNodeWithContentDescription(settingsIconContentDesc).assertExists()

            onNodeWithText(leaderboard).performClick()
            onNodeWithContentDescription(settingsIconContentDesc).assertExists()

            onNodeWithText(account).performClick()
            onNodeWithContentDescription(settingsIconContentDesc).assertExists()
        }
    }

    @Test
    fun settingsIconIsClicked_settingsDialogIsShown() {
        composeTestRule.apply {
            onNodeWithContentDescription(settingsIconContentDesc).performClick()

            onNodeWithText(settings).assertExists()
        }
    }
}
