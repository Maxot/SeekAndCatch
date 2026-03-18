package com.maxot.seekandcatch.core.common.model

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
            rowDuration = 700,
            lifeCount = 5,
            maxLifeCount = 5,
            itemsPassedWithoutMissToGetLife = 25,
            goalRotationIntervalMillis = 30_000L
        )
    ),
    NORMAL(
        GameParams(
            percentOfSuitableItem = 0.3f,
            coefficientStep = 0.2f,
            scorePoint = 15,
            rowWidth = 4,
            rowDuration = 700,
            lifeCount = 3,
            maxLifeCount = 5,
            itemsPassedWithoutMissToGetLife = 50,
            goalRotationIntervalMillis = 20_000L
        )
    ),
    HARD(
        GameParams(
            percentOfSuitableItem = 0.35f,
            coefficientStep = 0.1f,
            scorePoint = 20,
            rowWidth = 5,
            rowDuration = 700,
            lifeCount = 1,
            maxLifeCount = 5,
            itemsPassedWithoutMissToGetLife = 100,
            goalRotationIntervalMillis = 15_000L
        )
    )
}