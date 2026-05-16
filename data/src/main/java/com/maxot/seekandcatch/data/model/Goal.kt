package com.maxot.seekandcatch.data.model

/**
 * Represent goal in game process. Used to determine if user click on correct object in game.
 */
sealed class Goal<out T : Any> {
    abstract fun getGoal(): T

    class Colored(private val value: FigureColor) : Goal<FigureColor>() {
        override fun getGoal(): FigureColor {
            return value
        }
    }

    class Shaped(private val value: Figure.FigureType) : Goal<Figure.FigureType>() {
        override fun getGoal(): Figure.FigureType {
            return value
        }
    }
}
