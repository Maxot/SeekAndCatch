package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.maxot.seekandcatch.data.model.Figure
import com.maxot.seekandcatch.data.model.getShapeForFigure
import com.maxot.seekandcatch.feature.gameplay.R
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ColoredFigureLayoutTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var coloredFigureContentDesc: String

    private val figure = Figure(type = Figure.FigureType.CIRCLE, color = Color.Red)

    @Before
    fun setup() {
        composeTestRule.activity.apply {
            coloredFigureContentDesc = getString(R.string.colored_figure_content_desc, figure.id)
        }
    }

    @Test
    fun coloredFigureHasAlpha1f_performClick_alpha0f() {
        composeTestRule.setContent {
            MaterialTheme {
                ColoredFigureLayout(
                    figure = figure
                )
            }
        }
//        Espresso.onView(withContentDescription("Colored Figure")).check(matches(withAlpha(1f)))
//        composeTestRule.onNodeWithContentDescription("Colored Figure")
//            .performClick()
//        Espresso.onView(withContentDescription("Colored Figure")).check(matches(withAlpha(0f)))

        composeTestRule.onNodeWithContentDescription(coloredFigureContentDesc)
            .assert(SemanticsMatcher.expectValue(AlphaKey, 1f))
            .performClick()
            .assert(SemanticsMatcher.expectValue(AlphaKey, 0f))

    }

    @Test
    fun coloredFigure_colorAndShapeIsCorrect() {
        composeTestRule.setContent {
            MaterialTheme {
                ColoredFigureLayout(
                    figure = figure
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(coloredFigureContentDesc)
            .assertExists()
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNodeWithContentDescription(coloredFigureContentDesc)
            .assertBackgroundColor(figure.color!!)

        composeTestRule.onNodeWithContentDescription(coloredFigureContentDesc)
            .assert(SemanticsMatcher.expectValue(ShapeKey, figure.getShapeForFigure()))

    }

}

fun SemanticsNodeInteraction.assertBackgroundColor(expectedBackground: Color) {
    val capturedName = captureToImage().colorSpace.name
    Assert.assertEquals(expectedBackground.colorSpace.name, capturedName)
}
