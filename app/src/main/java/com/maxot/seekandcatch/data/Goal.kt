package com.maxot.seekandcatch.data

import androidx.compose.ui.graphics.Color

sealed class Goal<out T: Any> {
    abstract fun getGoal(): T

    class Colored(private val value: Color) : Goal<Color>() {
        override fun getGoal(): Color {
            return value
        }
    }
    class Figured(private val figure: Figure) : Goal<Figure>(){
        override fun getGoal(): Figure {
            return figure
        }
    }
}
