package com.maxot.seekandcatch.feature.gameplay.gameselection.model

import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode

sealed class GameSelectionUiEvent {
    data class ChangeGameMode(val gameMode: GameMode) : GameSelectionUiEvent()
    data class ChangeGameDifficult(val gameDifficult: GameDifficulty) : GameSelectionUiEvent()
}
