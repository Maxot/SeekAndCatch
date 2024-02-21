package com.maxot.seekandcatch.feature.gameplay.data

sealed class GameMode {
    object LevelsGameMode: GameMode()
    object FlowGameMode: GameMode()
}
