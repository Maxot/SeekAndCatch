package com.maxot.seekandcatch.feature.account.ui.model

import com.maxot.seekandcatch.core.common.base.BaseEvent
import com.maxot.seekandcatch.data.model.FigureColor

sealed interface AccountScreenEvent: BaseEvent {
    data class ChangeName(val name: String): AccountScreenEvent
    data class ChangeSelectedColors(val colors: Set<FigureColor>): AccountScreenEvent
}
