package com.maxot.seekandcatch.feature.account.ui.model

import androidx.compose.ui.graphics.Color
import com.maxot.seekandcatch.core.common.base.BaseEvent

sealed interface AccountScreenEvent: BaseEvent {
    data class ChangeName(val name: String): AccountScreenEvent
    data class ChangeSelectedColors(val colors: Set<Color>): AccountScreenEvent
}
