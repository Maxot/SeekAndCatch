package com.maxot.seekandcatch.feature.gameplay.gameselection.model

import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.data.model.GameMode

sealed class GameSelectionUiEvent {
    data class ChangeGameMode(val gameMode: GameMode) : GameSelectionUiEvent()
    data class ChangeGameDifficult(val gameDifficult: GameDifficulty) : GameSelectionUiEvent()
}
