package com.maxot.seekandcatch.feature.gameplay.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.maxot.seekandcatch.core.common.model.Figure
import com.maxot.seekandcatch.core.designsystem.getShapeForFigure
import com.maxot.seekandcatch.feature.gameplay.R
import com.maxot.seekandcatch.feature.gameplay.ui.layout.AlphaKey
import com.maxot.seekandcatch.feature.gameplay.ui.layout.ColoredFigureLayout
import com.maxot.seekandcatch.feature.gameplay.ui.layout.ShapeKey
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
        val figure = Figure(type = Figure.FigureType.CIRCLE, color = Color.Red)
        var clicked = false
        composeTestRule.setContent {
            MaterialTheme {
                ColoredFigureLayout(
                    figure = figure,
                    onItemClick = { 
                        clicked = true
                    }
                )
            }
        }

        val contentDesc = composeTestRule.activity.getString(R.string.colored_figure_content_desc, figure.id)
        
        composeTestRule.onNodeWithContentDescription(contentDesc)
            .assertExists()
            .performClick()

        Assert.assertTrue(clicked)
    }

    @Test
    fun coloredFigure_colorAndShapeIsCorrect() {
        val figure = Figure(type = Figure.FigureType.CIRCLE, color = Color.Red)
        composeTestRule.setContent {
            MaterialTheme {
                ColoredFigureLayout(
                    figure = figure
                )
            }
        }

        val contentDesc = composeTestRule.activity.getString(R.string.colored_figure_content_desc, figure.id)

        composeTestRule.onNodeWithContentDescription(contentDesc)
            .assertExists()
            .assertIsDisplayed()
            .assertHasClickAction()

        composeTestRule.onNodeWithContentDescription(contentDesc)
            .assertBackgroundColor(figure.color!!)

        composeTestRule.onNodeWithContentDescription(contentDesc)
            .assert(SemanticsMatcher.expectValue(ShapeKey, figure.getShapeForFigure()))
    }

}

fun SemanticsNodeInteraction.assertBackgroundColor(expectedBackground: Color) {
    val capturedName = captureToImage().colorSpace.name
    Assert.assertEquals(expectedBackground.colorSpace.name, capturedName)
}
