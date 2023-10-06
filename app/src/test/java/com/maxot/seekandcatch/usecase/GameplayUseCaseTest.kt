package com.maxot.seekandcatch.usecase

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.data.Figure
import com.maxot.seekandcatch.data.FigureType
import com.maxot.seekandcatch.data.Goal
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class GameplayUseCaseTest {
    private val gameplayUseCase = GameplayUseCase()

    @Test
    fun calculateCountOfSuitableFigures_fiveFiguresWithThreeCorrect_returnThree() {
        val figures = listOf<Figure>(
            Figure(FigureType.Circle, Color.Red),
            Figure(FigureType.Triangle, Color.Red),
            Figure(FigureType.Circle, Color.Blue),
            Figure(FigureType.Square, Color.Red),
            Figure(FigureType.Circle, Color.Yellow)
        )

        val goals = setOf<Goal<Any>>(Goal.Figured(Figure(FigureType.Circle, null)))

        assertEquals(gameplayUseCase.calculateCountOfSuitableFigures(figures, goals), 3)
    }

    @Test
    fun checkGoalCondition_triangleRed_goalTriangle_returnTrue() {
        val figure = Figure(FigureType.Triangle, Color.Red)

        val goals = setOf<Goal<Any>>(Goal.Figured(Figure(FigureType.Triangle, null)))
        gameplayUseCase.checkGoalCondition(goals, figure)
        assertEquals(gameplayUseCase.checkGoalCondition(goals, figure), true)
    }

    @Test
    fun checkGoalCondition_triangleRed_goalCircle_returnFalse() {
        val figure = Figure(FigureType.Triangle, Color.Red)

        val goals = setOf<Goal<Any>>(Goal.Figured(Figure(FigureType.Circle, null)))
        gameplayUseCase.checkGoalCondition(goals, figure)
        assertEquals(gameplayUseCase.checkGoalCondition(goals, figure), false)
    }

    @Test
    fun calculateFrameDuration_threeSuitableFigures_equal1600() {
        val oneFigureDuration = 200L
        val delayCoefficient = 1f
        val figures = listOf<Figure>(
            Figure(FigureType.Circle, Color.Red),
            Figure(FigureType.Triangle, Color.Red),
            Figure(FigureType.Circle, Color.Blue),
            Figure(FigureType.Square, Color.Red),
            Figure(FigureType.Circle, Color.Yellow)
        )
        val goals = setOf<Goal<Any>>(Goal.Figured(Figure(FigureType.Circle, null)))
        val frameDuration = gameplayUseCase.calculateFrameDuration(
            oneFigureDuration = oneFigureDuration,
            delayCoefficient = delayCoefficient,
            frameFigures = figures,
            goals = goals
        )

        assertEquals(frameDuration, 1600)
    }
}