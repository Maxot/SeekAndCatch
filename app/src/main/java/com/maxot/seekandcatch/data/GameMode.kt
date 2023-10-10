package com.maxot.seekandcatch.data

sealed class GameMode {
    object LevelsGameMode: GameMode()
    object FlowGameMode: GameMode()
}
