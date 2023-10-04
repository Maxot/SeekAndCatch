package com.maxot.seekandcatch.data

import androidx.compose.ui.graphics.Color

sealed class Goal {
    abstract fun getString(): String
    class Colored(val color: Color) : Goal() {

        override fun getString(): String {
            return when (color) {
                Color.Red -> "Red"
                else -> "Error"
            }
        }

    }
}
