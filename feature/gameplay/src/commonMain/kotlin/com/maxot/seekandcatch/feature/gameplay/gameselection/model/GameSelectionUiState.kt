package com.maxot.seekandcatch.feature.gameplay.gameselection.model

import com.maxot.seekandcatch.core.common.model.GameDifficulty
import com.maxot.seekandcatch.core.common.model.GameMode

data class GameSelectionUiState(
    val selectedDifficulty: GameDifficulty = GameDifficulty.NORMAL,
    val selectedGameMode: GameMode = GameMode.FLOW
)
