package com.maxot.seekandcatch.feature.account.ui.model

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.core.common.base.BaseUIState
import com.maxot.seekandcatch.core.common.model.User

data class AccountScreenUIState(
    val user: User? = null,
    val availableColors: Set<Color> = emptySet(),
    val selectedColors: Set<Color> = emptySet()
): BaseUIState()
