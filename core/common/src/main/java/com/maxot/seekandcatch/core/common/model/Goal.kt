package com.maxot.seekandcatch.core.common.model

import androidx.compose.ui.graphics.Color

sealed class Goal<out T : Any> {
    abstract fun getGoal(): T

    class Colored(private val value: Color) : Goal<Color>() {
        override fun getGoal(): Color {
            return value
        }
    }

    class Shaped(private val value: Figure.FigureType) : Goal<Figure.FigureType>() {
        override fun getGoal(): Figure.FigureType {
            return value
        }
    }

}
