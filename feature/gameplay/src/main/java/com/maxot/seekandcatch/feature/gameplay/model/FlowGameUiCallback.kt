package com.maxot.seekandcatch.feature.gameplay.model

sealed class FlowGameUiCallback {
    data class PointsAdded(val itemId: Int, val number: Int): FlowGameUiCallback()
}