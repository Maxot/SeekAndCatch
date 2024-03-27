package com.maxot.seekandcatch.feature.gameplay.data

@Deprecated("")
sealed class GameMode {
    object LevelsGameMode : GameMode()
    object FlowGameMode : GameMode()
}
