package com.maxot.seekandcatch.data.model

/**
 * Represent the common difficulty level of the game.
 * Each Difficulty is a different [GameParams] with different values.
 */
enum class GameDifficulty(val gameParams: GameParams) {
    EASY(
        GameParams(
            percentOfSuitableItem = 0.4f,
            coefficientStep = 0.25f,
            scorePoint = 10,
            rowWidth = 3,
            rowDuration = 700
        )
    ),
    NORMAL(
        GameParams(
            percentOfSuitableItem = 0.3f,
            coefficientStep = 0.2f,
            scorePoint = 15,
            rowWidth = 4,
            rowDuration = 700
        )
    ),
    HARD(
        GameParams(
            percentOfSuitableItem = 0.35f,
            coefficientStep = 0.1f,
            scorePoint = 20,
            rowWidth = 5,
            rowDuration = 700
        )
    )
}