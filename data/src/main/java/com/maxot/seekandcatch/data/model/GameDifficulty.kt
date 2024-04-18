package com.maxot.seekandcatch.data.model

/**
 * Represent the common difficulty level of the game.
 * Each Difficulty is a different [GameParams] with different values.
 */
enum class GameDifficulty(val gameParams: GameParams) {
    EASY(
        GameParams(
            percentOfSuitableItem = 0.2f,
            coefficientStep = 0.25f,
            scorePoint = 10,
            itemDuration = 50
        )
    ),
    NORMAL(
        GameParams(
            percentOfSuitableItem = 0.35f,
            coefficientStep = 0.2f,
            scorePoint = 15,
            itemDuration = 40
        )
    ),
    HARD(
        GameParams(
            percentOfSuitableItem = 0.5f,
            coefficientStep = 0.1f,
            scorePoint = 20,
            itemDuration = 30
        )
    )
}