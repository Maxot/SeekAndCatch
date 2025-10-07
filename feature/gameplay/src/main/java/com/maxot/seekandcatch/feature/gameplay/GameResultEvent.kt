package com.maxot.seekandcatch.feature.gameplay

sealed interface GameResultEvent {
    data object AddToLeaderboardClicked : GameResultEvent
    data object ContinueClicked : GameResultEvent
    data object DismissUserNameDialog : GameResultEvent
}
