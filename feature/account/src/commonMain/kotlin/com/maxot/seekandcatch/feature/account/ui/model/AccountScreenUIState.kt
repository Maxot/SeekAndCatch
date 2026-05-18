package com.maxot.seekandcatch.feature.account.ui.model

import com.maxot.seekandcatch.core.common.base.BaseUIState
import com.maxot.seekandcatch.core.common.model.User
import com.maxot.seekandcatch.data.model.FigureColor

data class AccountScreenUIState(
    val user: User? = null,
    val availableColors: Set<FigureColor> = emptySet(),
    val selectedColors: Set<FigureColor> = emptySet()
) : BaseUIState()
