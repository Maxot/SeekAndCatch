package com.maxot.seekandcatch.core.domain.model

sealed class FlowGameEvent {
    object StartGame : FlowGameEvent()
    object ResumeGame : FlowGameEvent()
    object PauseGame : FlowGameEvent()
    object FinishGame : FlowGameEvent()
    data class OnItemClick(val itemId: Int) : FlowGameEvent()
    data class FirstVisibleItemIndexChanged(val firstVisibleItemIndex: Int) : FlowGameEvent()
    data class ItemHeightMeasured(val height: Int) : FlowGameEvent()
    data object UpdateScrollDuration : FlowGameEvent()
    data object UpdatePixelsToScroll : FlowGameEvent()

}