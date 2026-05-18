package com.maxot.seekandcatch.feature.gameplay

sealed interface GameResultEvent {
    data object ContinueClicked : GameResultEvent
    data object RestartClicked : GameResultEvent
    data object NewBestSoundPlayed : GameResultEvent
}
