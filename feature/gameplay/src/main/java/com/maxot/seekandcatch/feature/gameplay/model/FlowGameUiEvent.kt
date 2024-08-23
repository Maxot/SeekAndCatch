package com.maxot.seekandcatch.feature.gameplay.model

sealed class FlowGameUiEvent {
    data class OnItemClick(val itemId: Int) : FlowGameUiEvent()
    data class FirstVisibleItemIndexChanged(val firstVisibleItemIndex: Int) : FlowGameUiEvent()
    data class ItemHeightMeasured(val height: Int) : FlowGameUiEvent()
    data object UpdateScrollDuration : FlowGameUiEvent()
    data object UpdatePixelsToScroll : FlowGameUiEvent()
    data object SetGameReadyToStart : FlowGameUiEvent()
    data object ResumeGame : FlowGameUiEvent()
    data object PauseGame : FlowGameUiEvent()
    data object FinishGame : FlowGameUiEvent()
}
