package com.maxot.seekandcatch.feature.gameplay.data.repository

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.feature.gameplay.data.Figure
import com.maxot.seekandcatch.feature.gameplay.data.FigureType
import kotlin.random.Random

interface FiguresRepository {
    fun createRandomFigure(): Figure

    fun generateFigures(itemsCount: Int): List<Figure>

    companion object {
        fun getRandomFigureType(seed: Int = getRandomNumber()): FigureType {
            return when (seed) {
                0 -> FigureType.Circle
                1 -> FigureType.Square
                2 -> FigureType.Triangle
                else -> FigureType.Circle
            }
        }

        fun getRandomColor(seed: Int = getRandomNumber()): Color {
            return when (seed) {
                0 -> Color.Red
                1 -> Color.Blue
                2 -> Color.Green
                3 -> Color.Yellow
                else -> Color.Red
            }
        }

        fun getRandomNumber(): Int {
            return Random.nextInt(4)
        }
    }
}