package com.maxot.seekandcatch.feature.gameplay.gameselection.model

import com.maxot.seekandcatch.data.model.GameDifficulty
import com.maxot.seekandcatch.data.model.GameMode

data class GameSelectionUiState(
    val selectedDifficulty: GameDifficulty = GameDifficulty.NORMAL,
    val selectedGameMode: GameMode = GameMode.FLOW
)
