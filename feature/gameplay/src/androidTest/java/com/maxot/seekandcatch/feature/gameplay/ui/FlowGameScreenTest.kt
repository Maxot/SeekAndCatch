package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.Goal
import com.maxot.seekandcatch.feature.gameplay.FlowGameUiState
import com.maxot.seekandcatch.feature.gameplay.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FlowGameScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var pauseDialogContentDesc: String
    private lateinit var flowGameScreenContentDesc: String
    private lateinit var goalsLayoutContentDesc: String
    private lateinit var coefficientProgressLayoutContentDesc: String
    private lateinit var gameFieldLayoutContentDesc: String

    private lateinit
    var scoreString: String

    private val goal = Goal.Colored(Color.Red)
    private val score = 155
    private val figures = listOf(
        Figure(id = 0, type = Figure.FigureType.CIRCLE, color = Color.Red),
        Figure(id = 1, type = Figure.FigureType.TRIANGLE, color = Color.Blue),
        Figure(id = 2, type = Figure.FigureType.SQUARE, color = Color.Yellow),
        Figure(id = 3, type = Figure.FigureType.CIRCLE, color = Color.Red),
        Figure(id = 4, type = Figure.FigureType.TRIANGLE, color = Color.Red),
        Figure(id = 5, type = Figure.FigureType.SQUARE, color = Color.Blue),
        Figure(id = 6, type = Figure.FigureType.CIRCLE, color = Color.Red),
        Figure(id = 7, type = Figure.FigureType.TRIANGLE, color = Color.Blue),
        Figure(id = 8, type = Figure.FigureType.SQUARE, color = Color.Yellow),
        Figure(id = 9, type = Figure.FigureType.CIRCLE, color = Color.Yellow),
    )

    @Before
    fun setup() {
        composeTestRule.activity.apply {
            pauseDialogContentDesc = getString(R.string.pause_dialog_content_desc)
            flowGameScreenContentDesc = getString(R.string.flow_game_screen_content_desc)
            goalsLayoutContentDesc = getString(R.string.goals_layout_content_desc)
            coefficientProgressLayoutContentDesc =
                getString(R.string.coefficient_progress_layout_content_desc)
            gameFieldLayoutContentDesc =
                getString(R.string.game_field_layout_content_desc)

            scoreString = getString(R.string.label_score, score)
        }
    }

    @Test
    fun activeState_displayCorrectly() {
        composeTestRule.setContent {
            FlowGameScreen(
                goals = setOf(goal),
                score = score,
                figures = figures,
                coefficient = 2.5f,
                flowGameUiState = FlowGameUiState.Active,
            )
        }

        composeTestRule.onNodeWithContentDescription(flowGameScreenContentDesc).assertIsDisplayed()

        composeTestRule.onNodeWithText(scoreString).assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription(goalsLayoutContentDesc).assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription(coefficientProgressLayoutContentDesc)
            .assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription(gameFieldLayoutContentDesc).onChildren()
            .assertCountEquals(figures.size)
    }

    @Test
    fun pauseState_pauseDialogShown() {
        composeTestRule.setContent {
            FlowGameScreen(
                goals = setOf(goal),
                score = score,
                figures = listOf(),
                coefficient = 2f,
                flowGameUiState = FlowGameUiState.Paused
            )
        }

        composeTestRule.onNodeWithContentDescription(pauseDialogContentDesc).assertIsDisplayed()
    }

}
