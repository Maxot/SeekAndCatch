package com.maxot.seekandcatch.core.domain.flash

sealed class FlashGameEvent {
    data object StartGame : FlashGameEvent()
    data object ResumeGame : FlashGameEvent()
    data object PauseGame : FlashGameEvent()
    data object FinishGame : FlashGameEvent()
    data class OnCellClick(val cellId: Int) : FlashGameEvent()
    data class Tick(val millis: Long) : FlashGameEvent()
    data object ResetGame : FlashGameEvent()
}