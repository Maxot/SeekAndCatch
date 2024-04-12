package com.maxot.seekandcatch.data.model

enum class GameDifficulty(val gameParams: GameParams) {
    EASY(GameParams(percentOfSuitableItem = 0.25f)),
    NORMAL(GameParams(percentOfSuitableItem = 0.5f)),
    HARD(GameParams(percentOfSuitableItem = 0.75f))
}